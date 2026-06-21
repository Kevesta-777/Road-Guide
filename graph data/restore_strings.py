#!/usr/bin/env python3
import subprocess
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent

base = subprocess.check_output(
    ["git", "show", "HEAD:road-guide-kotlin/app/src/main/res/values/strings.xml"],
    cwd=REPO_ROOT,
    text=True,
    errors="replace",
)
gold = """
    <!-- Gold Hunt -->
    <string name="gold_hunt_title">Gold Hunt</string>
    <string name="gold_hunt_credits_earned">+%1$d credits</string>
    <string name="gold_hunt_treasure_collected">Collected treasure! +%1$d credits</string>
    <string name="gold_hunt_treasure_already_collected">Already collected</string>
    <string name="gold_hunt_treasure_zoom_required">Zoom in to discover treasures and secret places, please.</string>
    <string name="gold_hunt_routing_required">Check network connection or option offline routing, please.</string>
    <string name="gold_hunt_workflow_intro">At first, Zoom in enough, you\'ll see mysterious treasures and hunt!</string>
    <string name="gold_hunt_workflow_first_star">Congratulation! You gained first gold star!\\nIf you earn enough gold stars, You\'ll see new treasure-Flowers! Enjoy!</string>
    <string name="gold_hunt_workflow_first_flower">Congratulation! You gained first flower!\\nIf you reach mission, You\'ll see new treasure-Crystal! Enjoy!</string>
    <string name="gold_hunt_workflow_first_crystal">Congratulation! You gained first crystal!\\nIf you reach mission, You\'ll see new treasure-Gift! Enjoy!</string>
    <string name="gold_hunt_workflow_first_gift">Congratulation! You gained first gift!\\nIf you reach mission, You\'ll see new treasure-Secret place! Enjoy!</string>
    <string name="gold_hunt_workflow_first_secret">Congratulation! You gained first secret place!\\nIf you reach mission, You\'ll see new treasure-Secret place! Enjoy!</string>
    <string name="gold_hunt_workflow_hoard_announced">I\'m going to provide place that huge treasure hidden.\\n%1$s</string>
    <string name="gold_hunt_bar_total_credits">TOTAL CREDITS:</string>
    <string name="gold_hunt_bar_log">LOG:</string>
    <string name="gold_hunt_bar_log_empty">—</string>
    <string name="gold_hunt_secret_not_revealed">Collect enough gifts to reveal secret places</string>
"""
out = base.replace("</resources>", gold + "\n</resources>")
out_path = REPO_ROOT / "road-guide-kotlin/app/src/main/res/values/strings.xml"
out_path.write_text(out, encoding="utf-8")
print(f"Wrote {out_path} ({len(out.splitlines())} lines)")
