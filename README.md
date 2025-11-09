# 🗑️ The Filled Unit Notifier (FUN)

The **Filled Unit Notifier (FUN)** is a smart trash management system that helps janitorial staff monitor, reset, and receive updates on trash bin levels in real time.
Built for **BCIT**, this project combines **IoT hardware**, **Flask backend**, and **SMS integration** to make waste tracking simple, efficient, and automated.

This project was made with 4 total members, with 1 being in term 1.

---

## 📖 Overview

FUN makes it easier to know when trash bins are full — without manually checking them.
Each bin uses a **Raspberry Pi** and **ultrasonic sensor** to measure fill levels and send automatic updates to a **Flask server**.
Janitors can reset bins with a button press and receive real-time notifications through text messages.

This system is designed for **BCIT Secure** and **Tall Timbers Wi-Fi** networks.

## Inspiration

We were sitting in the Tall Timber Room and saw a janitor open a trash bin, see that it was empty, and then leave. We thought — what if they could just message their phone, like the TransLink app, and sign up to get notified when a trash can is full? They could also see every bin in a building and its status.

## What It Does

Our app scans a trash can and measures whether it is full or not based on ultrasonic sensors. It then sends the bin’s status to a Flask server, which updates our database with this information. Users can message a number and subscribe to certain bins to receive notifications every 20 minutes about their status, or send a request to see all bins’ statuses or a specific bin’s status.

## How We Built It

We used a Raspberry Pi and ultrasonic sensors for the hardware side. We used a Flask server and Twilio for real-time messaging, and a PostgreSQL database to keep track of the current status of each trash can along with the trashcans users would receive notifications about. We also used Java to create a terminal-based UI for interacting with the PostgreSQL database.

## Challenges We Ran Into

**Hardware Challenges**`<br>`
Our sensors sometimes sent false readings due to the reflection inside the bin. We used a counter to ensure the value was consistent during a 15-second phase to confirm whether the bin was empty or full.

**Software Challenges**`<br>`
We ran into issues where our trashcans, phone messaging service, and our SQL database could not communicate with each other. The issue became really bad when we had the servers on different networks, and became impossible when connecting to the BCIT secure network. This was solved by using ngrok, a service used to forward certain ports through an online service, and the issue disappeared completely.

**Edge Cases**

1. We get bad readings sometimes the reading can be too low or high, due to hallucinations or trash being inserted but falling. We set up a counter and a timer for 10 seconds. It checks when we get a reading if it falls within a conditon such as high or low we will increment count. If it hits 5 within 10 seconds we know the reading is most likely true so we can change to full.
2. We didn't want to run out of credits/oversend data to the api in flask. We solved this by adding a button to the raspberrypi. If the bin state is full stay full and when the button is clicked while chainging the grabage it will reset state back to empty.
3. Sometimes, notifications get sent out, read, and forgot about. We solved this by sending out a new notification every 20 seconds for debugging, and would be 20 minutes for production.

## Accomplishments That We’re Proud Of

We built a reliable system to communicate with our phones and get live data. We found a way to save time by turning off the Raspberry Pi while trash is being changed and restarting it afterward. We also added a clean terminal-based UI/UX for the database. A janitor even told us this would help them a lot and gave us props.

## What We Learned

We learned how to use Postman and test API calls. We also learned how four people can work on different components and still stay on the same page.

## What’s Next for the FUN

We want to upgrade our sensors to more reliable models. We also plan to add more devices and collect additional data to improve our project. We would love to make the system cheaper and more resilient so more of them could be deployed at a much lower cost. We would also love to make the back-end system more reliable and easier to setup so anyone could deploy a FUN.`<br><br>``<br>`More technical documentation can be found within the GitHub readme.

---

## 🧰 Installation Guide

Follow these steps carefully — you don’t need any programming knowledge to get started. These steps are made for Windows computers, but may be adapted for other systems.

---

### 💻 Step 1: Flask & Backend Setup

**!! Only do this step if an admin has not yet set it up for your network !!**

##### **Setting up the code and environment variables**

**Download the repository and setup .env file**

1. Download the repository
   ![1762680706351](image/README/1762680706351.png)
2. Extract the files to a memorable location, such as your Documents folder
3. Open the folder, and navigate to the "SMS Backend" directory
4. Create a new file named ".env" without quotes, and make sure there is no other file extension
5. Open the .env file using any text editor

**Twilio:**

1. Create an account on [Twilio](https://www.twilio.com/try-twilio)
2. Press the Get a Phone Number button, and then click Close to register a trial number
3. Locate your Account SID, Auth Token, and Twilio Phone number located at the bottom of the page
4. Create 3 new entries in your .env file named TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER, and copy your data into those entries

**PostgresSQL**

1. Download the Postgres SQL server installer from [here](https://www.postgresql.org/download/)
2. Follow [this guide](https://www.instaclustr.com/education/postgresql/postgresql-tutorial-get-started-with-postgresql-in-4-easy-steps/#sec-3) to setup a Postgres SQL server, making sure to remember your host, port, username, and password for the server
3. Create 4 new entries in your .env file named SQL_HOST, SQL_PORT, SQL_USER, and SQL_PASSWORD, and put your data into those entries
4. Within your newly created database, run the schema.sql file located in the "psql" directory in this repository
5. Optionally, run the seeding.sql file if you do not already have data for your database (this is only for testing purposes, all data can be cleared once everything is setup)

In the end, your .env file will look something like this:

![1762681854574](image/README/1762681854574.png)

##### **Running the Flask Server**

1. Install Python from the [Microsoft Store](https://apps.microsoft.com/detail/9pnrbtzxmb4z?hl=en-US&gl=US) or from [Python&#39;s website](https://www.python.org/downloads/)
2. Open the terminal and change directory into the "SMS Backend" folder
3. Run ``pip install -r requirements.txt``` and wait for the installer to finish
4. To start the server, run ``flask run`` in the terminal

---

### 🗑️ Step 2: Setting Up the Trash Bin

You’ll be provided with a trash bin that already has all software, sensors and hardware installed, including:

- **A Raspberry Pi with Code Installed**
- **Ultrasonic Sensor**
- **Red LED indicator**
- **Reset Button**

#### 🔧 Resetting the Bin

1. After you empty the trash, **press the button once.**
2. Wait for a second, then **press the button again.**
3. Look for the **red LED light** — if it **turns off and then back on**, the reset worked.
4. The bin is now ready for use again.

> 💡 Tip: You’ll hear a small click each time you press the button — that’s normal!
> Note: after resetting, wait 40 seconds before messaging to check status

---

### 📱 Step 3: Using the SMS Command System

The system is controlled entirely through **text messages** — no app or software needed.

#### 📩 Getting Started

1. Text the system phone number provided to you.
2. To see all available commands, text **`F`**.
   You’ll receive a message listing every command you can use.

#### 💬 Common Commands

| Command                  | Description                              | Example              |
| ------------------------ | ---------------------------------------- | -------------------- |
| `F`                    | Lists all available commands             | `F`                |
| `subscribe bin [ID]`   | Subscribes to updates for a specific bin | `subscribe bin 3`  |
| `status bin [ID]`      | Shows whether the bin is full or empty   | `status bin 3`     |
| `list [building_name]` | Lists all bins in a specific building    | `list talltimbers` |

- In **demo mode**, updates are sent **every 20 seconds**.
- In **production**, updates are sent **every 20 minutes**.

---

---

### 🧹 Step 4: Resetting After Trash Collection

When janitors empty a bin, they must reset it so the system knows it’s empty.

#### 🔁 Reset Process

1. Locate the **reset button** under the bin lid.
2. **Press it once**, wait for a click.
3. **Press it again**, wait for another click.
4. The **red LED** will flash to confirm the reset.
5. Wait about **40 seconds**, then text:
   The reply should say the bin is **empty (0)**.

---

### 🗄️ Step 5: PostgreSQL Database & Java TUI Setup

The Java TUI (Terminal User Interface) allows you to manage trashcan data directly in the database through a terminal interface.

#### 📋 Prerequisites

Before running the Java TUI, make sure you have:

- **Java Development Kit (JDK) 11 or higher** installed
  - Check your version: `java -version`
  - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
- **PostgreSQL JDBC Driver** (included in the project)
- **Database connection credentials** (provided by us)

#### 📦 Dependencies

The Java TUI uses **Gradle** to manage dependencies automatically. The following are included:

- **PostgreSQL JDBC Driver** (version 42.7.3)
  - Automatically downloaded and managed by Gradle
  - No manual installation required

#### 🔧 Database Setup

The database is already set up and accessible through the provided connection. You don't need to create or configure the database yourself — just connect using the credentials provided by us.

#### 🚀 Running the Java TUI

You have two options for running the Java TUI:

##### Option A: Building and Running the JAR (For Development)

This project uses **Gradle** to build the application. Gradle automatically downloads and manages all dependencies, including the PostgreSQL JDBC driver.

1. **Navigate to the project directory:**

   ```bash
   cd HTTP-HACKS-2025
   ```
2. **Build the JAR file (downloads dependencies and compiles):**

   ```bash
   ./gradlew jar
   ```

   This will automatically download the PostgreSQL JDBC driver and compile all Java files into a runnable JAR.
3. **Run the TUI:**

   ```bash
   java -jar build/libs/TrashcanTUI.jar
   ```

   > 💡 **Note:** The first time you build, it may take a few minutes to download dependencies. Subsequent builds will be much faster.
   >

#### 🔌 Connecting to the Database

When you start the TUI, you'll see a menu. Select **option 1** to connect to the database.

Enter the following connection details:

- **DB URL:** ``jdbc:postgresql://0.tcp.us-cal-1.ngrok.io:12025/trash_db``
- **Username:** (Provided by us)
- **Password:** (Provided by us)

> 💡 **Note:** The database connection is already configured. You only need the username and password credentials to connect.

#### 📖 Using the Java TUI

Once connected, you'll see the main menu:

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/selectMenu.png?raw=true)

**Available Operations:**

| Option                                | Description                                   | Details                                                                     |
| ------------------------------------- | --------------------------------------------- | --------------------------------------------------------------------------- |
| **1. Connect to Database**      | Establishes connection to PostgreSQL          | Enter URL, username, and password                                           |
| **2. View Trashcans**           | Displays all trashcans in a formatted table   | Shows fresh data from database + unsaved entries                            |
| **3. Add Trashcan**             | Creates a new trashcan entry (in memory)      | Enter name, location, status (0-100%), building, floor, latitude, longitude |
| **4. Update Trashcan**          | Modifies an existing trashcan                 | Enter ID, then update name, location, or status                             |
| **5. Delete Trashcan**          | Removes a trashcan from the database          | Requires confirmation before deletion                                       |
| **6. Save Changes to Database** | Commits all in-memory changes to the database | Saves new entries, updates, and deletions                                   |
| **7. Exit**                     | Closes the application                        | Warns if there are unsaved changes                                          |

#### Viewing Trashcans

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/tableView.png?raw=true)

- **Status** is stored as a **percentage (currently only 0 or 100)** representing fill level:
  - `0` = Empty
  - `100` = Full

When viewing trashcans, status is displayed as `##%` format.
You can select a specific trashcan to view it's detailed information by entering it's ID
![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/detailedView.png?raw=true)

#### Add Trashcan

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/addNewEntry.png?raw=true)

#### Update Trashcan

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/modifyEntry.png?raw=true)

#### Delete Trashcan

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/deleteEntry.png?raw=true)

#### Save Changes to Database

![alt text](https://github.com/StanislavRudenkoko/HTTP-HACKS-2025/blob/master/images/saveChanges.png?raw=true)

#### ⚠️ Important Notes

- **Unsaved Changes:** The TUI works with an in-memory copy of data. Changes are only saved when you select **option 6**.
- **Preview IDs:** New entries show preview IDs (what the ID will be after saving). These are listed in the warning message at the bottom of the table.
- **Fresh Data:** Option 2 (View Trashcans) always fetches the latest data from the database, so you'll see the most up-to-date information.
- **Connection:** You must connect to the database (option 1) before using any other features.

#### 🐛 Troubleshooting

**Connection Issues:**

- Verify your username and password are correct (contact us if needed)
- Ensure you have an active internet connection (the database is accessed remotely)
- If the connection fails, the ngrok tunnel may be inactive — contact us

**Compilation Errors:**

- Ensure JDK is installed: `java -version` (should show version 11 or higher)
- Make sure Gradle is working: `./gradlew --version` (or `gradlew.bat --version` on Windows)
- If dependencies fail to download, check your internet connection
- Verify all Java files are in `src/main/java/`

**Runtime Errors:**

- If using `./gradlew run`, dependencies are automatically included — no need to manually set classpath
- Verify you're using the exact database URL: `jdbc:postgresql://6.tcp.us-cal-1.ngrok.io:17425/trash_db`
- Double-check your username and password credentials
- If you see "ClassNotFoundException" for PostgreSQL driver, try running `./gradlew build` again to ensure dependencies are downloaded

---
