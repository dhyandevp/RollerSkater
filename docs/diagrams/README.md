# Architecture & System Diagrams

This directory contains standalone [Mermaid](https://mermaid.js.org/) specification diagrams visualizing the internal subsystems, concurrency pipelines, and data flow of **RollerSkater**.

---

## Diagram Index

| File | Type | Description |
|---|---|---|
| [`system_architecture.mmd`](system_architecture.mmd) | Flowchart | High-level system architecture spanning Presentation, Service, Data, and Native layers. |
| [`mediastore_sync.mmd`](mediastore_sync.mmd) | Sequence Diagram | Android MediaStore indexing, TagLib native metadata extraction, and Room FTS4 triggers. |
| [`crossfade_lifecycle.mmd`](crossfade_lifecycle.mmd) | Sequence Diagram | DualPlayerEngine dual-player handover and trigonometric equal-power ($\sin/\cos$) crossfade loop. |
| [`backup_restore.mmd`](backup_restore.mmd) | Sequence Diagram | On-device JSON/`.rsk` export and non-destructive restoration via Storage Access Framework (SAF). |

---

## Rendering Diagrams

These `.mmd` files can be rendered using:

1. **GitHub Markdown Preview**: Mermaid diagrams render natively inside fenced code blocks (` ```mermaid `) in GitHub markdown files.
2. **Mermaid CLI (`@mermaid-js/mermaid-cli`)**:
   ```bash
   # Install CLI
   npm install -g @mermaid-js/mermaid-cli

   # Render to SVG
   mmdc -i system_architecture.mmd -o system_architecture.svg

   # Render to PNG
   mmdc -i system_architecture.mmd -o system_architecture.png -b transparent
   ```
3. **Live Interactive Viewer**: Copy and paste `.mmd` content directly into [Mermaid Live Editor](https://mermaid.live/).
