import sys

path = r"d:\project\eden-nutrition\docs\SECKILL_MANAGEMENT_PHASE1_REPORT.md"

with open(path, "r", encoding="utf-8") as f:
    text = f.read()

# SPLIT BY "## Phase 2: Service Layer & Business Logic Implementation (Completed)"
parts = text.split("## Phase 2: Service Layer & Business Logic Implementation (Completed)")
if len(parts) > 2:
    clean_text = parts[0] + "## Phase 2: Service Layer & Business Logic Implementation (Completed)" + parts[1]
    with open(path, "w", encoding="utf-8") as f:
        f.write(clean_text)

print("Report fixed")