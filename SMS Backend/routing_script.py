import os
from dotenv import load_dotenv
import psycopg
from tabulate import tabulate
from math import radians, cos, sin, asin, sqrt

# Load environment variables
load_dotenv()


# --------------------------
# Database Connection
# --------------------------
def connect_db():
    try:
        conn = psycopg.connect(
            host=os.environ["SQL_HOST"],
            port=os.environ["SQL_PORT"],
            dbname="trash_db",
            user=os.environ["SQL_USER"],
            password=os.environ["SQL_PASSWORD"]
        )
        return conn
    except Exception as e:
        print("Failed to connect to DB:", e)
        return None


# --------------------------
# Fetch Trashcans
# --------------------------
def fetch_full_trashcans(conn, threshold=100):
    """Fetch trashcans with status >= threshold."""
    with conn.cursor() as cur:
        cur.execute("""
                    SELECT id,
                           name,
                           building,
                           floor,
                           latitude,
                           longitude,
                           status,
                           last_updated
                    FROM smart_trashcan.trashcans
                    WHERE status >= %s
                    ORDER BY building, floor, name;
                    """, (threshold,))
        return cur.fetchall()


# --------------------------
# Helper: Distance between coordinates
# --------------------------
def haversine(lat1, lon1, lat2, lon2):
    """Calculate the great-circle distance in meters between two points."""
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * asin(sqrt(a))
    r = 6371000  # Radius of Earth in meters
    return c * r


# --------------------------
# Build Simple Route
# --------------------------
def build_route(trashcans):
    """Simple nearest-neighbor route for janitor."""
    if not trashcans:
        return []

    remaining = trashcans.copy()
    route = [remaining.pop(0)]  # start from first trashcan

    while remaining:
        last = route[-1]
        nearest = min(remaining, key=lambda x: haversine(last[4], last[5], x[4], x[5]))
        route.append(nearest)
        remaining.remove(nearest)

    return route


# --------------------------
# Display Functions
# --------------------------
def display_trashcans(trashcans):
    headers = ["ID", "Name", "Building", "Floor", "Latitude", "Longitude", "Status", "Last Updated"]
    print(tabulate(trashcans, headers=headers, tablefmt="grid"))


# --------------------------
# Format Route
# --------------------------
def format_route(route):
    """Returns a formatted string for the janitor's route, with dashes replaced by spaces."""
    if not route:
        return "No full trashcans to route."

    lines = ["=== Optimized Janitor Route ==="]
    for idx, t in enumerate(route, start=1):
        name_with_spaces = t[1].replace("-", " ")
        lines.append(f"{idx}. {name_with_spaces}")

    return "\n".join(lines)


# --------------------------
# Modular function for SMS
# --------------------------
def get_route_text_from_cursor(cur, building = None, threshold=75):
    """
    Accepts a psycopg cursor and returns a formatted route string.
    Can be called from another module to send SMS.
    :param cur for db cursor
    :param building the building to filter, can be none to show all trashcan route
    :param threshold  percentage after which a can is considered full
    """
    building_filter = f"AND LOWER(building)='{building}'" if building != None else ""
    cur.execute(f"""
                SELECT id,
                       name,
                       building,
                       floor,
                       latitude,
                       longitude,
                       status,
                       last_updated
                FROM smart_trashcan.trashcans
                WHERE status >= %s {building_filter}
                ORDER BY building, floor, name;
                """, (threshold,))
    trashcans = cur.fetchall()
    route = build_route(trashcans)
    return format_route(route)


# --------------------------
# Main
# --------------------------
def main():
    conn = connect_db()
    if not conn:
        return

    try:
        full_trashcans = fetch_full_trashcans(conn)
        print("\n--- Full Trashcans ---")
        display_trashcans(full_trashcans)

        route_text = format_route(build_route(full_trashcans))
        print("\n" + route_text)

    finally:
        conn.close()


if __name__ == "__main__":
    main()
