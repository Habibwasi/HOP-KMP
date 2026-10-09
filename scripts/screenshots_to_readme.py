"""
Builds the README screen gallery from Roborazzi preview renders.

    ./gradlew :composeApp:recordRoborazziDebug     # renders every @Preview → composeApp/screenshots/
    python scripts/screenshots_to_readme.py        # resizes into docs/screenshots/ and rewrites README

Requires Pillow (pip install pillow).
"""
import re
import shutil
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
RAW = ROOT / "composeApp" / "screenshots"
OUT = ROOT / "docs" / "screenshots"
README = ROOT / "README.md"
START, END = "<!-- screenshots:start -->", "<!-- screenshots:end -->"
WIDTH = 360
COLUMNS = 4

# Sections in the order a user meets them; screens listed in flow order, unlisted ones go last.
SECTIONS = [
    ("auth", "Onboarding & account", [
        "Onboarding", "SignUp", "Login", "VerifyEmail", "EmailVerified", "ForgotPassword",
    ]),
    ("shared", "Home, chat & profile", [
        "Home", "Notifications", "Chat", "OwnProfile", "OtherProfile", "Settings",
    ]),
    ("passenger", "Passenger", [
        "PassengerHome", "SearchResults", "TripDetail", "BookingConfirmation", "BookingSuccess",
        "MyTripsPassenger", "TripDetailActive", "CancellationConfirmation", "RateDriver",
    ]),
    ("driver", "Driver", [
        "DriverHome", "ReviewPending", "CarDetails", "EnableDriverMobilepay", "PostTripModelSelect",
        "PostTripModelA", "PostTripModelB", "PriceReview", "MyTripsDriver", "TripDetailActiveDriver",
        "MarkTripComplete", "RatePassenger", "TaxDashboard", "TaxReportDownload",
    ]),
    ("settlement", "After the ride: payment", [
        "PassengerSettlement", "DriverSettlement", "PastTripDetailDriver",
    ]),
]
# Reusable components are not pages; keep only the full-screen error state.
COMPONENTS_KEPT = {"Error"}
TITLE_FIXES = {"Mobilepay": "MobilePay", "Model A": "Model A", "Model B": "Model B"}


def words(camel: str) -> str:
    text = re.sub(r"(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", " ", camel).strip()
    for wrong, right in TITLE_FIXES.items():
        text = text.replace(wrong, right)
    return text


def parse(path: Path):
    """com.example.hop.ui.screens.<area>.<File>Kt.<fn>[.<preview name>][_WITH_BACKGROUND…].png"""
    stem = path.name.removeprefix("com.example.hop.ui.").removesuffix(".png")
    stem = re.sub(r"_?WITH_BACKGROUND.*$", "", stem)
    parts = stem.split(".")
    if parts[0] == "screens":
        area, file_kt, fn, rest = parts[1], parts[2], parts[3], ".".join(parts[4:])
    else:
        area, file_kt, fn, rest = "components", parts[1], parts[2], ".".join(parts[3:])

    screen = file_kt.removesuffix("Kt").removesuffix("Screen")
    if rest:
        state = rest.replace("_", " ").strip()
        state = re.sub(r"^[A-Z]{2}(-[A-Z]{2})?-\d+[a-z]?\s*[—–-]\s*", "", state)  # drop spec ids like "DR-09 — "
        state = re.sub(rf"^{re.escape(words(screen))}\s*[—–-]\s*", "", state)
    else:
        state = fn.removesuffix("Preview").removeprefix("Preview")
        for prefix in (file_kt.removesuffix("Kt"), screen):
            state = state.replace(prefix, "", 1) if state.startswith(prefix) else state
        state = words(state.removeprefix("Screen"))
    if not state or state.lower() == words(screen).lower():
        state = "Default"
    return area, screen, state[0].upper() + state[1:]


def slug(text: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def main():
    shots = {}
    for png in sorted(RAW.glob("*.png")):
        area, screen, state = parse(png)
        if area == "components":
            if screen not in COMPONENTS_KEPT:
                continue
            area = "shared"
        shots.setdefault(area, []).append((screen, state, png))

    if OUT.exists():
        shutil.rmtree(OUT)
    lines = [START, ""]
    total = 0
    for i, (area, title, order) in enumerate(SECTIONS):
        items = shots.get(area, [])
        if not items:
            continue
        rank = {name: n for n, name in enumerate(order)}
        items.sort(key=lambda s: (rank.get(s[0], len(order)), s[0], s[1] != "Default", s[1]))
        screens = len({s[0] for s in items})

        (OUT / area).mkdir(parents=True, exist_ok=True)
        cells = []
        for screen, state, png in items:
            dest = OUT / area / f"{slug(words(screen))}--{slug(state)}.png"
            with Image.open(png) as im:
                im = im.convert("RGB")
                im.resize((WIDTH, round(im.height * WIDTH / im.width)), Image.LANCZOS).save(dest, optimize=True)
            rel = dest.relative_to(ROOT).as_posix()
            cells.append(
                f'<td align="center" valign="top" width="25%"><img src="{rel}" width="180" alt="{words(screen)} — {state}"><br>'
                f"<b>{words(screen)}</b><br><sub>{state}</sub></td>"
            )
        total += len(items)

        lines.append(f"<details{' open' if i == 0 else ''}>")
        lines.append(f"<summary><b>{title}</b> — {screens} screens, {len(items)} states</summary>\n")
        lines.append("<table>")
        for r in range(0, len(cells), COLUMNS):
            lines.append("<tr>" + "".join(cells[r:r + COLUMNS]) + "</tr>")
        lines.append("</table>\n</details>\n")
    lines.append(END)

    readme = README.read_text(encoding="utf-8")
    block = "\n".join(lines)
    if START in readme:
        readme = re.sub(re.escape(START) + r".*?" + re.escape(END), lambda _: block, readme, flags=re.S)
    else:
        intro = (
            "## App Screens\n\n"
            "Every screen state below is rendered from the Android app's Compose `@Preview` functions "
            "(Pixel 5, Robolectric) — the real UI with sample data, not mockups. To refresh:\n\n"
            "```bash\n./gradlew :composeApp:recordRoborazziDebug\npython scripts/screenshots_to_readme.py\n```\n\n"
        )
        readme = readme.replace("## Tech Stack", f"{intro}{block}\n\n---\n\n## Tech Stack", 1)
    README.write_text(readme, encoding="utf-8")
    print(f"{total} screenshots -> {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
