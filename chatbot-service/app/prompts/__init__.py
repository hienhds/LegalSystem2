from pathlib import Path

classification_prompt = Path(
    "app/prompts/classification.prompt"
).read_text(encoding="utf-8")

contextualize_prompt = Path(
    "app/prompts/contextualize.prompt"
).read_text(encoding="utf-8")
