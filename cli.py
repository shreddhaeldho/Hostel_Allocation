#!/usr/bin/env python3
"""
Interactive Terminal CLI for Smart Hostel Room Allocation System.
Supports Admin Portal and Student Portal with colorized interactive menus.
"""

import sys
import json
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080/api"

# ANSI Terminal Color Helpers
GREEN = "\033[92m"
CYAN = "\033[96m"
YELLOW = "\033[93m"
RED = "\033[91m"
BOLD = "\033[1m"
RESET = "\033[0m"

def api_call(method, endpoint, payload=None):
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}
    data = json.dumps(payload).encode("utf-8") if payload is not None else None

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        try:
            parsed = json.loads(body)
        except Exception:
            parsed = {"message": body or e.reason}
        return e.code, parsed
    except urllib.error.URLError as e:
        print(f"\n{RED}{BOLD}[!] Connection Error:{RESET} Could not connect to Spring Boot backend at {BASE_URL}.")
        print(f"{YELLOW}Make sure the backend is running with:{RESET} ./mvnw spring-boot:run\n")
        return None, None

def check_backend():
    req = urllib.request.Request("http://localhost:8080/", method="GET")
    try:
        with urllib.request.urlopen(req, timeout=2) as resp:
            return resp.status == 200
    except Exception:
        return False

# ==================== ADMIN ACTIONS ====================

def admin_add_room():
    print(f"\n{CYAN}{BOLD}--- Add New Hostel Room ---{RESET}")
    print("Choose Room Type:")
    print("  1) Single (1 Bed)")
    print("  2) Double (2 Beds)")
    print("  3) Triple (3 Beds)")
    print("  4) Deluxe AC (3 Beds)")
    print("  5) Custom Type")
    choice = input(f"{BOLD}Select option (1-5): {RESET}").strip()

    preset_map = {
        "1": ("Single", 1),
        "2": ("Double", 2),
        "3": ("Triple", 3),
        "4": ("Deluxe AC", 3)
    }

    if choice in preset_map:
        room_type, default_cap = preset_map[choice]
        cap_input = input(f"Enter capacity [{default_cap}]: ").strip()
        capacity = int(cap_input) if cap_input else default_cap
    elif choice == "5":
        room_type = input("Enter custom room type name: ").strip()
        capacity = int(input("Enter room capacity (number of beds): ").strip())
    else:
        print(f"{RED}Invalid selection.{RESET}")
        return

    status, resp = api_call("POST", "/rooms", {"roomType": room_type, "capacity": capacity})
    if status == 201:
        print(f"\n{GREEN}{BOLD}[SUCCESS] Room Created!{RESET}")
        print(f"  Room ID      : {resp.get('id')}")
        print(f"  Room Type    : {resp.get('roomType')}")
        print(f"  Capacity     : {resp.get('capacity')} beds")
        print(f"  Occupied     : {resp.get('occupiedBeds')} beds")
    else:
        print(f"\n{RED}[ERROR] {resp.get('message', 'Failed to create room')}{RESET}")

def admin_view_rooms():
    print(f"\n{CYAN}{BOLD}--- Hostel Rooms Inventory ---{RESET}")
    status, rooms = api_call("GET", "/rooms")
    if status == 200:
        if not rooms:
            print(f"{YELLOW}No rooms configured yet. Add some rooms first!{RESET}")
            return
        print(f"{'Room ID':<10} {'Type':<18} {'Capacity':<10} {'Occupied':<10} {'Available':<10}")
        print("-" * 60)
        for r in rooms:
            avail = r['capacity'] - r['occupiedBeds']
            avail_str = f"{GREEN}{avail}{RESET}" if avail > 0 else f"{RED}FULL{RESET}"
            print(f"{r['id']:<10} {r['roomType']:<18} {r['capacity']:<10} {r['occupiedBeds']:<10} {avail_str}")
        print("-" * 60)
    else:
        print(f"{RED}[ERROR] Failed to fetch rooms.{RESET}")

def admin_view_allocations():
    print(f"\n{CYAN}{BOLD}--- Active Room Allocations ---{RESET}")
    status, allocs = api_call("GET", "/allocations")
    if status == 200:
        if not allocs:
            print(f"{YELLOW}No active allocations at this time.{RESET}")
            return
        print(f"{'Alloc ID':<10} {'Student ID':<12} {'Student Name':<20} {'Dept':<12} {'Room ID':<10} {'Type':<12}")
        print("-" * 78)
        for a in allocs:
            print(f"{a['allocationId']:<10} {a['studentId']:<12} {a['studentName']:<20} {a['department']:<12} {a['roomId']:<10} {a['roomType']:<12}")
        print("-" * 78)
    else:
        print(f"{RED}[ERROR] Failed to fetch allocations.{RESET}")

def admin_vacate_room():
    print(f"\n{CYAN}{BOLD}--- Vacate Room for a Student ---{RESET}")
    student_id = input(f"{BOLD}Enter Student ID to vacate: {RESET}").strip()
    if not student_id:
        return
    status, resp = api_call("DELETE", f"/allocations/{student_id}")
    if status == 200:
        print(f"\n{GREEN}{BOLD}[SUCCESS] {resp.get('message')}{RESET}")
        print(f"  Student ID           : {resp.get('studentId')}")
        print(f"  Student Name         : {resp.get('studentName')}")
        print(f"  Room ID Vacated      : {resp.get('roomId')}")
        print(f"  Remaining Occupants  : {resp.get('updatedOccupiedBeds')}")
    else:
        print(f"\n{RED}[ERROR] {resp.get('message', 'Failed to vacate room')}{RESET}")

def admin_seed_sample_data():
    print(f"\n{CYAN}{BOLD}--- Seeding Sample Hostel Data ---{RESET}")
    rooms = [
        {"roomType": "Single", "capacity": 1},
        {"roomType": "Single", "capacity": 1},
        {"roomType": "Double", "capacity": 2},
        {"roomType": "Double", "capacity": 2},
        {"roomType": "Deluxe AC", "capacity": 3}
    ]
    for r in rooms:
        s, resp = api_call("POST", "/rooms", r)
        if s == 201:
            print(f"  {GREEN}✔ Created {resp['roomType']} Room #{resp['id']} (Capacity: {resp['capacity']}){RESET}")
    print(f"{GREEN}{BOLD}Sample rooms successfully seeded!{RESET}")

def admin_menu():
    while True:
        print(f"\n{BOLD}{CYAN}========================================{RESET}")
        print(f"{BOLD}{CYAN}         HOSTEL ADMIN PORTAL            {RESET}")
        print(f"{BOLD}{CYAN}========================================{RESET}")
        print("  1. Add New Room")
        print("  2. View All Rooms (Inventory & Capacity)")
        print("  3. View All Active Allocations")
        print("  4. Vacate Room for a Student (Check-Out)")
        print("  5. Quick Seed Sample Rooms (5 Rooms)")
        print("  6. Return to Main Menu")
        choice = input(f"\n{BOLD}Select an action (1-6): {RESET}").strip()

        if choice == "1":
            admin_add_room()
        elif choice == "2":
            admin_view_rooms()
        elif choice == "3":
            admin_view_allocations()
        elif choice == "4":
            admin_vacate_room()
        elif choice == "5":
            admin_seed_sample_data()
        elif choice == "6":
            break
        else:
            print(f"{RED}Invalid option. Please choose 1-6.{RESET}")

# ==================== STUDENT ACTIONS ====================

def student_portal():
    print(f"\n{BOLD}{GREEN}========================================{RESET}")
    print(f"{BOLD}{GREEN}      STUDENT ROOM BOOKING PORTAL       {RESET}")
    print(f"{BOLD}{GREEN}========================================{RESET}")

    # Fetch available room types directly from backend
    status, rooms = api_call("GET", "/rooms")
    if status != 200 or not rooms:
        print(f"{RED}[!] No rooms are available in the system yet. Ask Admin to add rooms first!{RESET}")
        return

    # Collect available types with free beds
    available_types = sorted(list(set(r['roomType'] for r in rooms if r['capacity'] > r['occupiedBeds'])))
    all_types = sorted(list(set(r['roomType'] for r in rooms)))

    print(f"\n{BOLD}Please answer the following registration questions:{RESET}")
    name = input("  1. Enter your full name       : ").strip()
    if not name:
        print(f"{RED}Name cannot be empty.{RESET}")
        return

    department = input("  2. Enter your department (e.g. CSE, ECE, ME): ").strip()
    if not department:
        print(f"{RED}Department cannot be empty.{RESET}")
        return

    print("\n  3. Select your Preferred Room Type:")
    for idx, t in enumerate(all_types, start=1):
        has_space = t in available_types
        space_tag = f"{GREEN}(Beds Available){RESET}" if has_space else f"{RED}(Currently Full){RESET}"
        print(f"     [{idx}] {t:<15} {space_tag}")

    choice_str = input(f"\n{BOLD}Choose option number (1-{len(all_types)}): {RESET}").strip()
    try:
        choice_idx = int(choice_str) - 1
        if 0 <= choice_idx < len(all_types):
            preferred_type = all_types[choice_idx]
        else:
            print(f"{RED}Invalid option.{RESET}")
            return
    except ValueError:
        print(f"{RED}Invalid number.{RESET}")
        return

    # Step 1: Register the student
    print(f"\n{CYAN}Registering student in the database...{RESET}")
    status, student_resp = api_call("POST", "/students", {
        "name": name,
        "department": department,
        "preferredRoomType": preferred_type
    })

    if status != 201:
        print(f"{RED}[ERROR] Registration failed: {student_resp.get('message')}{RESET}")
        return

    student_id = student_resp.get("id")
    print(f"{GREEN}✔ Student Registered! Generated Student ID: {student_id}{RESET}")

    # Step 2: Request Smart Allocation
    print(f"{CYAN}Checking real-time bed availability & allocating best room...{RESET}")
    alloc_status, alloc_resp = api_call("POST", f"/allocations/{student_id}")

    if alloc_status == 201:
        print(f"\n{GREEN}{BOLD}===================================================={RESET}")
        print(f"{GREEN}{BOLD}      🎉 ROOM ALLOCATION SUCCESSFUL!               {RESET}")
        print(f"{GREEN}{BOLD}===================================================={RESET}")
        print(f"  Allocation ID    : {alloc_resp.get('allocationId')}")
        print(f"  Student Name     : {alloc_resp.get('studentName')} (ID: {alloc_resp.get('studentId')})")
        print(f"  Department       : {alloc_resp.get('department')}")
        print(f"  Allocated Room   : Room #{alloc_resp.get('roomId')} ({alloc_resp.get('roomType')})")
        print(f"  Room Status      : {alloc_resp.get('occupiedBeds')} / {alloc_resp.get('capacity')} beds occupied")
        print(f"{GREEN}{BOLD}===================================================={RESET}\n")
    elif alloc_status == 409:
        print(f"\n{YELLOW}{BOLD}[ALLOCATION CONFLICT]{RESET}")
        print(f"{YELLOW}{alloc_resp.get('message')}{RESET}")
        print(f"Tip: You are registered as Student ID #{student_id}. You can try again when beds are vacated.\n")
    else:
        print(f"\n{RED}[ERROR] {alloc_resp.get('message', 'Allocation failed')}{RESET}\n")

# ==================== MAIN APPLICATION ====================

def main():
    if not check_backend():
        print(f"\n{RED}{BOLD}[!] Backend Not Detected!{RESET}")
        print(f"{YELLOW}The Spring Boot backend server is not running on port 8080.{RESET}")
        print(f"Please start the backend in another terminal with:")
        print(f"   {GREEN}./mvnw spring-boot:run{RESET}\n")
        sys.exit(1)

    while True:
        print(f"\n{BOLD}{CYAN}================================================={RESET}")
        print(f"{BOLD}{CYAN}    SMART HOSTEL ALLOCATION TERMINAL CONSOLE     {RESET}")
        print(f"{BOLD}{CYAN}================================================={RESET}")
        print("  1. Admin Portal (Manage Rooms & View Status)")
        print("  2. Student Portal (Interactive Questionnaire & Booking)")
        print("  3. Exit")
        choice = input(f"\n{BOLD}Select role (1-3): {RESET}").strip()

        if choice == "1":
            admin_menu()
        elif choice == "2":
            student_portal()
        elif choice == "3":
            print(f"\n{CYAN}Goodbye!{RESET}\n")
            break
        else:
            print(f"{RED}Invalid selection. Please choose 1, 2, or 3.{RESET}")

if __name__ == "__main__":
    main()
