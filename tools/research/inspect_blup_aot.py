"""Read ELF64 Dart snapshot symbols and bounded package URI metadata; never execute the target."""
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import struct


def inspect(path):
    data = path.read_bytes()

    def unpack(fmt, offset):
        if offset < 0 or offset + struct.calcsize(fmt) > len(data):
            raise ValueError('ELF field outside artifact')
        return struct.unpack_from(fmt, data, offset)

    def region(offset, size):
        if offset < 0 or size < 0 or offset + size > len(data):
            raise ValueError('ELF region outside artifact')
        return data[offset:offset + size]

    if data[:6] != b'\x7fELF\x02\x01':
        raise ValueError('Expected ELF64 little endian')
    machine = unpack('<H', 18)[0]
    shoff = unpack('<Q', 40)[0]
    shentsize, shnum, shstrndx = unpack('<HHH', 58)
    if shentsize != 64 or not 0 < shnum < 65535 or shstrndx >= shnum:
        raise ValueError('Unsupported section table')
    sections = [unpack('<IIQQQQIIQQ', shoff + i * shentsize) for i in range(shnum)]
    names = region(sections[shstrndx][4], sections[shstrndx][5])

    def cstring(blob, offset):
        if not 0 <= offset < len(blob):
            raise ValueError('String offset outside table')
        end = blob.find(b'\0', offset)
        if end < 0:
            raise ValueError('Unterminated string')
        return blob[offset:end].decode('utf-8', errors='replace')

    snapshots = []
    for sec in sections:
        if sec[1] != 11:  # SHT_DYNSYM
            continue
        if sec[6] >= len(sections) or sec[9] != 24 or sec[5] % 24:
            raise ValueError('Invalid dynamic symbol table')
        strings_sec = sections[sec[6]]
        strings = region(strings_sec[4], strings_sec[5])
        for offset in range(sec[4], sec[4] + sec[5], 24):
            name, info, other, index, address, size = unpack('<IBBHQQ', offset)
            symbol = cstring(strings, name)
            if not symbol.startswith('_kDart'):
                continue
            if not 0 < index < len(sections):
                raise ValueError('Dart symbol lacks file-backed section')
            owner = sections[index]
            relative = address - owner[3]
            if owner[1] == 8 or relative < 0 or relative + size > owner[5]:
                raise ValueError('Dart symbol extends beyond file-backed section')
            file_offset = owner[4] + relative
            payload = region(file_offset, size)
            snapshots.append(dict(name=symbol, address=hex(address), fileOffset=file_offset,
                                  bytes=size, section=cstring(names, owner[0]),
                                  sha256=hashlib.sha256(payload).hexdigest()))
    isolate = next((s for s in snapshots if s['name'] == '_kDartIsolateSnapshotData'), None)
    if isolate is None:
        raise ValueError('Isolate snapshot data symbol missing')
    pool = region(isolate['fileOffset'], isolate['bytes'])
    uris = {}
    for match in re.finditer(rb'package:[A-Za-z0-9_./-]+\.dart', pool):
        uri = match.group().decode('ascii')
        uris.setdefault(uri, isolate['fileOffset'] + match.start())
    selected = [dict(uri=uri, firstByteOffset=offset) for uri, offset in sorted(uris.items())
                if uri.startswith('package:visual_builder/') and re.search(
                    r'undo|redo|save|load|code.?gen|smart_layout|guiding|node_point|figma|library|variable|debug|flow', uri)]
    return dict(method='ELF64 section/dynamic-symbol parsing and literal URI probes inside isolate data',
                artifact=dict(bytes=len(data), sha256=hashlib.sha256(data).hexdigest(), machine=machine),
                snapshots=snapshots, packageUriCount=len(uris),
                packages=dict(Counter(uri.split('/')[0] for uri in uris).most_common()),
                selectedSourceUris=selected[:180], selectedSourceUriTotal=len(selected),
                limitations=['URIs are embedded byte-pattern observations, not recovered source files',
                             'Names do not prove execution, class semantics or dependency versions',
                             'This does not deserialize Dart snapshot clusters or decompile Dart functions'])


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('artifact', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    result = json.dumps(inspect(args.artifact), indent=2, ensure_ascii=False) + '\n'
    if args.output:
        args.output.write_text(result, encoding='utf-8')
    else:
        print(result)
