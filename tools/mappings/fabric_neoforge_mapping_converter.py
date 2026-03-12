#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import gzip
import io
import os
import sys
import urllib.request
import zipfile
from dataclasses import dataclass
from typing import Dict, Iterable, List, Optional, Tuple


@dataclass(frozen=True)
class MappingEntry:
    kind: str  # class, field, method
    owner_named: str
    name_named: str
    desc_named: str
    owner_official: str
    name_official: str
    desc_official: str


@dataclass(frozen=True)
class InputEntry:
    kind: str  # class, field, method
    owner: str
    name: str
    desc: str
    owner_raw: str
    name_raw: str
    desc_raw: str
    line_no: int


def open_text(path: str):
    if path.endswith(".gz"):
        return io.TextIOWrapper(gzip.open(path, "rb"), encoding="utf-8")
    return open(path, "r", encoding="utf-8")


def parse_header(path: str) -> Tuple[List[str], int, int]:
    with open_text(path) as f:
        for line in f:
            line = line.rstrip("\n")
            if not line or line.startswith("#"):
                continue
            parts = line.split("\t")
            if parts[0] != "tiny":
                raise ValueError("Invalid tiny mapping file: missing header line.")
            if len(parts) < 5:
                raise ValueError("Invalid tiny header: not enough fields.")
            namespaces = parts[3:]
            if namespaces[0] not in ("official", "mojang"):
                raise ValueError(
                    "Unsupported tiny file: first namespace must be 'official' or 'mojang' "
                    "to safely remap descriptors."
                )
            try:
                named_idx = namespaces.index("named")
            except ValueError as exc:
                raise ValueError("Tiny file does not include a 'named' namespace.") from exc
            if "official" in namespaces:
                official_idx = namespaces.index("official")
            else:
                official_idx = namespaces.index("mojang")
            return namespaces, official_idx, named_idx
    raise ValueError("Invalid tiny mapping file: empty or missing header.")


def build_class_maps(
    path: str, official_idx: int, named_idx: int
) -> Tuple[Dict[str, str], Dict[str, str]]:
    named_to_official: Dict[str, str] = {}
    official_to_named: Dict[str, str] = {}
    with open_text(path) as f:
        for line in f:
            if not line or line.startswith("#"):
                continue
            raw = line.rstrip("\n")
            if not raw or raw.startswith("tiny"):
                continue
            parts = raw.split("\t")
            if parts[0] != "c":
                continue
            names = parts[1:]
            if max(official_idx, named_idx) >= len(names):
                continue
            official = names[official_idx]
            named = names[named_idx]
            if named and official:
                named_to_official[named] = official
                official_to_named[official] = named
    return named_to_official, official_to_named


def remap_descriptor(desc: str, class_map: Dict[str, str]) -> str:
    if not desc:
        return desc
    out = []
    i = 0
    length = len(desc)
    while i < length:
        c = desc[i]
        if c == "L":
            j = desc.find(";", i)
            if j == -1:
                out.append(desc[i:])
                break
            name = desc[i + 1 : j]
            mapped = class_map.get(name, name)
            out.append("L" + mapped + ";")
            i = j + 1
        else:
            out.append(c)
            i += 1
    return "".join(out)


def normalize_class(name: str) -> str:
    return name.replace(".", "/") if name else name


def normalize_desc(desc: str) -> str:
    return desc.replace(".", "/") if desc else desc


def format_owner(name: str, dot: bool) -> str:
    if not name:
        return ""
    return name.replace("/", ".") if dot else name


def parse_input_entries(path: str) -> List[InputEntry]:
    entries: List[InputEntry] = []
    with open(path, "r", encoding="utf-8") as f:
        raw_lines = f.readlines()
    # Find first non-empty, non-comment line for format detection.
    first_data = None
    for line in raw_lines:
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        first_data = stripped
        break
    if first_data is None:
        return entries
    is_csv = False
    delimiter = ","
    if ("," in first_data or "\t" in first_data) and any(
        token in first_data.lower() for token in ("kind", "owner", "name", "desc")
    ):
        is_csv = True
        delimiter = "\t" if "\t" in first_data else ","
    if is_csv:
        with open(path, "r", encoding="utf-8", newline="") as f:
            reader = csv.DictReader(f, delimiter=delimiter)
            for idx, row in enumerate(reader, start=2):
                row_norm = {str(k).strip().lower(): (v or "") for k, v in row.items() if k}
                kind_raw = str(row_norm.get("kind", "")).strip()
                owner_raw = str(row_norm.get("owner", "")).strip()
                name_raw = str(row_norm.get("name", "")).strip()
                desc_raw = str(row_norm.get("desc", "")).strip()
                if not kind_raw or not name_raw:
                    continue
                kind = normalize_kind(kind_raw)
                entries.append(
                    InputEntry(
                        kind=kind,
                        owner=normalize_class(owner_raw),
                        name=normalize_class(name_raw) if kind == "class" else name_raw,
                        desc=normalize_desc(desc_raw),
                        owner_raw=owner_raw,
                        name_raw=name_raw,
                        desc_raw=desc_raw,
                        line_no=idx,
                    )
                )
    else:
        for idx, line in enumerate(raw_lines, start=1):
            stripped = line.strip()
            if not stripped or stripped.startswith("#"):
                continue
            parts = stripped.split()
            if not parts:
                continue
            kind = normalize_kind(parts[0])
            if kind == "class":
                if len(parts) < 2:
                    continue
                name_raw = parts[1]
                entries.append(
                    InputEntry(
                        kind=kind,
                        owner="",
                        name=normalize_class(name_raw),
                        desc="",
                        owner_raw="",
                        name_raw=name_raw,
                        desc_raw="",
                        line_no=idx,
                    )
                )
            else:
                if len(parts) < 3:
                    continue
                owner_raw = parts[1]
                name_raw = parts[2]
                desc_raw = parts[3] if len(parts) >= 4 else ""
                entries.append(
                    InputEntry(
                        kind=kind,
                        owner=normalize_class(owner_raw),
                        name=name_raw,
                        desc=normalize_desc(desc_raw),
                        owner_raw=owner_raw,
                        name_raw=name_raw,
                        desc_raw=desc_raw,
                        line_no=idx,
                    )
                )
    return entries


def normalize_kind(value: str) -> str:
    v = value.strip().lower()
    if v in ("c", "class"):
        return "class"
    if v in ("f", "field"):
        return "field"
    if v in ("m", "method"):
        return "method"
    return v


def download_yarn(yarn_version: str, cache_dir: str) -> str:
    os.makedirs(cache_dir, exist_ok=True)
    jar_name = f"yarn-{yarn_version}-v2.jar"
    jar_path = os.path.join(cache_dir, jar_name)
    tiny_path = os.path.join(cache_dir, f"yarn-{yarn_version}-v2.tiny")
    if not os.path.exists(jar_path):
        url = f"https://maven.fabricmc.net/net/fabricmc/yarn/{yarn_version}/{jar_name}"
        print(f"Downloading {url}", file=sys.stderr)
        urllib.request.urlretrieve(url, jar_path)
    with zipfile.ZipFile(jar_path, "r") as zf:
        with zf.open("mappings/mappings.tiny") as zf_entry, open(
            tiny_path, "wb"
        ) as out_f:
            out_f.write(zf_entry.read())
    return tiny_path


def build_member_maps(
    path: str,
    official_idx: int,
    named_idx: int,
    official_to_named: Dict[str, str],
) -> Tuple[
    Dict[Tuple[str, str, str], MappingEntry],
    Dict[Tuple[str, str], List[MappingEntry]],
    Dict[Tuple[str, str, str], MappingEntry],
    Dict[Tuple[str, str], List[MappingEntry]],
]:
    field_by_named: Dict[Tuple[str, str, str], MappingEntry] = {}
    field_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]] = {}
    field_by_official: Dict[Tuple[str, str, str], MappingEntry] = {}
    field_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]] = {}
    method_by_named: Dict[Tuple[str, str, str], MappingEntry] = {}
    method_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]] = {}
    method_by_official: Dict[Tuple[str, str, str], MappingEntry] = {}
    method_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]] = {}

    current_class_official = ""
    current_class_named = ""

    with open_text(path) as f:
        for line in f:
            if not line or line.startswith("#"):
                continue
            raw = line.rstrip("\n")
            if not raw or raw.startswith("tiny"):
                continue
            parts = raw.split("\t")
            if parts[0] == "c":
                names = parts[1:]
                if max(official_idx, named_idx) >= len(names):
                    continue
                current_class_official = names[official_idx]
                current_class_named = names[named_idx]
                continue
            if parts[0] != "":
                continue
            if len(parts) < 4:
                continue
            kind = parts[1]
            if kind not in ("f", "m"):
                continue
            desc_official = parts[2]
            names = parts[3:]
            if max(official_idx, named_idx) >= len(names):
                continue
            name_official = names[official_idx]
            name_named = names[named_idx]
            desc_named = remap_descriptor(desc_official, official_to_named)
            entry = MappingEntry(
                kind="field" if kind == "f" else "method",
                owner_named=current_class_named,
                name_named=name_named,
                desc_named=desc_named,
                owner_official=current_class_official,
                name_official=name_official,
                desc_official=desc_official,
            )
            if kind == "f":
                key_named = (current_class_named, name_named, desc_named)
                key_named_nod = (current_class_named, name_named)
                key_off = (current_class_official, name_official, desc_official)
                key_off_nod = (current_class_official, name_official)
                field_by_named[key_named] = entry
                field_by_official[key_off] = entry
                field_by_named_nod.setdefault(key_named_nod, []).append(entry)
                field_by_official_nod.setdefault(key_off_nod, []).append(entry)
            else:
                key_named = (current_class_named, name_named, desc_named)
                key_named_nod = (current_class_named, name_named)
                key_off = (current_class_official, name_official, desc_official)
                key_off_nod = (current_class_official, name_official)
                method_by_named[key_named] = entry
                method_by_official[key_off] = entry
                method_by_named_nod.setdefault(key_named_nod, []).append(entry)
                method_by_official_nod.setdefault(key_off_nod, []).append(entry)
    return (
        field_by_named,
        field_by_named_nod,
        field_by_official,
        field_by_official_nod,
        method_by_named,
        method_by_named_nod,
        method_by_official,
        method_by_official_nod,
    )


def guess_namespace(
    owner: str, named_to_official: Dict[str, str], official_to_named: Dict[str, str]
) -> str:
    if owner in named_to_official:
        return "named"
    if owner in official_to_named:
        return "official"
    return "unknown"


def lookup_member(
    kind: str,
    owner: str,
    name: str,
    desc: str,
    named_to_official: Dict[str, str],
    official_to_named: Dict[str, str],
    field_by_named: Dict[Tuple[str, str, str], MappingEntry],
    field_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]],
    field_by_official: Dict[Tuple[str, str, str], MappingEntry],
    field_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]],
    method_by_named: Dict[Tuple[str, str, str], MappingEntry],
    method_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]],
    method_by_official: Dict[Tuple[str, str, str], MappingEntry],
    method_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]],
) -> Tuple[Optional[MappingEntry], str, str]:
    ns = guess_namespace(owner, named_to_official, official_to_named)
    if kind == "field":
        map_named = field_by_named
        map_named_nod = field_by_named_nod
        map_off = field_by_official
        map_off_nod = field_by_official_nod
    else:
        map_named = method_by_named
        map_named_nod = method_by_named_nod
        map_off = method_by_official
        map_off_nod = method_by_official_nod

    def exact_lookup(m, o, n, d):
        if not d:
            return None
        return m.get((o, n, d))

    entry = None
    if ns == "named":
        entry = exact_lookup(map_named, owner, name, desc)
        if entry is None:
            entry = exact_lookup(map_off, owner, name, desc)
    elif ns == "official":
        entry = exact_lookup(map_off, owner, name, desc)
        if entry is None:
            entry = exact_lookup(map_named, owner, name, desc)
    else:
        entry = exact_lookup(map_named, owner, name, desc)
        if entry is None:
            entry = exact_lookup(map_off, owner, name, desc)

    if entry is not None:
        return entry, "ok", ""

    # Fallback: try without descriptor if unique.
    candidates: List[MappingEntry] = []
    if ns == "named":
        candidates = map_named_nod.get((owner, name), [])
        if not candidates:
            candidates = map_off_nod.get((owner, name), [])
    elif ns == "official":
        candidates = map_off_nod.get((owner, name), [])
        if not candidates:
            candidates = map_named_nod.get((owner, name), [])
    else:
        candidates = map_named_nod.get((owner, name), [])
        if not candidates:
            candidates = map_off_nod.get((owner, name), [])
    if len(candidates) == 1:
        return candidates[0], "ok", "descriptor_missing_used_unique_candidate"
    if len(candidates) > 1:
        return None, "ambiguous", f"{len(candidates)} candidates; add descriptor"
    return None, "not_found", "no match"


def write_full_output(
    tiny_path: str,
    official_idx: int,
    named_idx: int,
    official_to_named: Dict[str, str],
    out_path: str,
    dot: bool,
    only_kinds: Iterable[str],
):
    only = set(only_kinds)
    if out_path == "-":
        out_f = sys.stdout
    else:
        out_f = open(out_path, "w", encoding="utf-8", newline="")
    writer = csv.writer(out_f, lineterminator="\n")
    writer.writerow(
        [
            "kind",
            "owner_named",
            "name_named",
            "desc_named",
            "owner_official",
            "name_official",
            "desc_official",
        ]
    )

    current_class_official = ""
    current_class_named = ""

    with open_text(tiny_path) as f:
        for line in f:
            if not line or line.startswith("#"):
                continue
            raw = line.rstrip("\n")
            if not raw or raw.startswith("tiny"):
                continue
            parts = raw.split("\t")
            if parts[0] == "c":
                names = parts[1:]
                if max(official_idx, named_idx) >= len(names):
                    continue
                current_class_official = names[official_idx]
                current_class_named = names[named_idx]
                if "class" in only:
                    writer.writerow(
                        [
                            "class",
                            "",
                            format_owner(current_class_named, dot),
                            "",
                            "",
                            format_owner(current_class_official, dot),
                            "",
                        ]
                    )
                continue
            if parts[0] != "":
                continue
            if len(parts) < 4:
                continue
            kind = parts[1]
            if kind not in ("f", "m"):
                continue
            desc_official = parts[2]
            names = parts[3:]
            if max(official_idx, named_idx) >= len(names):
                continue
            name_official = names[official_idx]
            name_named = names[named_idx]
            desc_named = remap_descriptor(desc_official, official_to_named)
            out_kind = "field" if kind == "f" else "method"
            if out_kind not in only:
                continue
            writer.writerow(
                [
                    out_kind,
                    format_owner(current_class_named, dot),
                    name_named,
                    desc_named,
                    format_owner(current_class_official, dot),
                    name_official,
                    desc_official,
                ]
            )
    if out_f is not sys.stdout:
        out_f.close()


def convert_input(
    input_entries: List[InputEntry],
    named_to_official: Dict[str, str],
    official_to_named: Dict[str, str],
    field_by_named: Dict[Tuple[str, str, str], MappingEntry],
    field_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]],
    field_by_official: Dict[Tuple[str, str, str], MappingEntry],
    field_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]],
    method_by_named: Dict[Tuple[str, str, str], MappingEntry],
    method_by_named_nod: Dict[Tuple[str, str], List[MappingEntry]],
    method_by_official: Dict[Tuple[str, str, str], MappingEntry],
    method_by_official_nod: Dict[Tuple[str, str], List[MappingEntry]],
    out_path: str,
    dot: bool,
):
    if out_path == "-":
        out_f = sys.stdout
    else:
        out_f = open(out_path, "w", encoding="utf-8", newline="")
    writer = csv.writer(out_f, lineterminator="\n")
    writer.writerow(
        [
            "kind",
            "owner_in",
            "name_in",
            "desc_in",
            "owner_named",
            "name_named",
            "desc_named",
            "owner_official",
            "name_official",
            "desc_official",
            "status",
            "note",
        ]
    )
    for entry in input_entries:
        if entry.kind == "class":
            if entry.name in named_to_official:
                official = named_to_official[entry.name]
                named = entry.name
                status = "ok"
                note = ""
            elif entry.name in official_to_named:
                named = official_to_named[entry.name]
                official = entry.name
                status = "ok"
                note = ""
            else:
                named = ""
                official = ""
                status = "not_found"
                note = "no match"
            writer.writerow(
                [
                    "class",
                    "",
                    entry.name_raw,
                    "",
                    "",
                    format_owner(named, dot),
                    "",
                    "",
                    format_owner(official, dot),
                    "",
                    status,
                    note,
                ]
            )
            continue

        found, status, note = lookup_member(
            entry.kind,
            entry.owner,
            entry.name,
            entry.desc,
            named_to_official,
            official_to_named,
            field_by_named,
            field_by_named_nod,
            field_by_official,
            field_by_official_nod,
            method_by_named,
            method_by_named_nod,
            method_by_official,
            method_by_official_nod,
        )
        if found is None:
            writer.writerow(
                [
                    entry.kind,
                    entry.owner_raw,
                    entry.name_raw,
                    entry.desc_raw,
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    status,
                    note,
                ]
            )
            continue
        writer.writerow(
            [
                entry.kind,
                entry.owner_raw,
                entry.name_raw,
                entry.desc_raw,
                format_owner(found.owner_named, dot),
                found.name_named,
                found.desc_named,
                format_owner(found.owner_official, dot),
                found.name_official,
                found.desc_official,
                status,
                note,
            ]
        )
    if out_f is not sys.stdout:
        out_f.close()


def parse_only(value: str) -> List[str]:
    allowed = {"class", "field", "method"}
    items = [v.strip().lower() for v in value.split(",") if v.strip()]
    for item in items:
        if item not in allowed:
            raise argparse.ArgumentTypeError(f"Invalid kind: {item}")
    return items


def main() -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Convert Fabric (Yarn/named) mappings to NeoForge (official) using a tiny v2 mapping file."
        )
    )
    parser.add_argument("--tiny", help="Path to yarn tiny v2 mapping file (or .gz).")
    parser.add_argument(
        "--yarn",
        help="Yarn version to download (e.g. 1.21.1+build.1). Overrides --tiny.",
    )
    parser.add_argument(
        "--cache-dir",
        default=os.path.join("tools", "mappings", ".cache"),
        help="Cache directory for downloaded yarn mappings.",
    )
    parser.add_argument("--input", help="Input list to convert (CSV/TSV or simple text).")
    parser.add_argument(
        "--out",
        default="mapping_output.csv",
        help="Output CSV file path, or '-' for stdout.",
    )
    parser.add_argument(
        "--dot",
        action="store_true",
        help="Output class/owner names in dot format (descriptors remain slash format).",
    )
    parser.add_argument(
        "--only",
        type=parse_only,
        default=["class", "field", "method"],
        help="Comma-separated kinds to export when no --input is provided.",
    )

    args = parser.parse_args()

    if not args.tiny and not args.yarn:
        parser.error("You must provide --tiny or --yarn.")

    tiny_path = args.tiny
    if args.yarn:
        tiny_path = download_yarn(args.yarn, args.cache_dir)

    namespaces, official_idx, named_idx = parse_header(tiny_path)

    named_to_official, official_to_named = build_class_maps(
        tiny_path, official_idx, named_idx
    )

    if args.input:
        input_entries = parse_input_entries(args.input)
        (
            field_by_named,
            field_by_named_nod,
            field_by_official,
            field_by_official_nod,
            method_by_named,
            method_by_named_nod,
            method_by_official,
            method_by_official_nod,
        ) = build_member_maps(tiny_path, official_idx, named_idx, official_to_named)

        convert_input(
            input_entries,
            named_to_official,
            official_to_named,
            field_by_named,
            field_by_named_nod,
            field_by_official,
            field_by_official_nod,
            method_by_named,
            method_by_named_nod,
            method_by_official,
            method_by_official_nod,
            args.out,
            args.dot,
        )
    else:
        write_full_output(
            tiny_path,
            official_idx,
            named_idx,
            official_to_named,
            args.out,
            args.dot,
            args.only,
        )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
