# Jmix Dynamic Model Demo

## Overview

The project includes two static entities: `User` and `Department`. The following entities and enumerations will be created at runtime: 

- Dynamic entities:
  - `City`: `name`, `country`
  - `Office`: `name`, `address`, `capacity`, `city`, `meetingRooms`
  - `MeetingRoom`: `name`, `seats`
- Dynamic enumerations:
  - `MaritalStatus`: `SINGLE`, `MARRIED`, `DIVORCED`
- Dynamic attributes of static entities:
  - `Department`: `office`
  - `User`: `maritalStatus`, `phone`

## Demo Scenario

1. Run the application and go to <http://localhost:8109> in your browser.
2. Log in as `admin` with password `admin`.
3. Open _Dynamic model → Dynamic model settings_ view.
4. Switch to _Code_ in the top right corner and replace the editor content with the code from [demo.yml](demo.yml).
5. Click _Apply_, then _Apply changes_.
6. Refresh the browser page.
7. Explore the new _Cities_, _Offices_ views and new attributes of the `User` entity.
8. Login as `bob` with password `1`. You will get read-only access to the `City`, `Office` and `MeetingRoom` dynamic entities.
9. Login as `alice` with password `1`. You will get full access to all dynamic entities and attributes.