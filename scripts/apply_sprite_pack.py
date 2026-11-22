#!/usr/bin/env python3
import json, os, shutil, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
cfg_path = os.path.join(ROOT, "sprite-pack.json")
cfg = json.load(open(cfg_path, encoding="utf-8"))

pack = sys.argv[1] if len(sys.argv) > 1 else cfg["activePack"]
if pack not in cfg["packs"]:
    print("Unknown sprite pack:", pack)
    print("Available packs:", list(cfg["packs"].keys()))
    sys.exit(1)

pack_dir = os.path.join(ROOT, "assets-packs", pack)
assets_dir = os.path.join(ROOT, "src", "main", "resources", "view", "assets")
os.makedirs(assets_dir, exist_ok=True)

copied = 0
for fn in os.listdir(pack_dir):
    if fn.endswith((".png", ".jpg")):
        shutil.copy2(os.path.join(pack_dir, fn), os.path.join(assets_dir, fn))
        copied += 1

cfg["activePack"] = pack
json.dump(cfg, open(cfg_path, "w", encoding="utf-8"), indent=2)
print(f"Applied sprite pack '{pack}' ({copied} files) -> {assets_dir}")
