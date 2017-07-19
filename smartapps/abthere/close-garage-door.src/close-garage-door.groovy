definition(
    name: "Close Garage Door",
    namespace: "abthere",
    author: "abthere",
    description: "Close Garage Door at certain time.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences { 
    section("Select Garage Door Sensor...") {
		input name: "sensor", type: "capability.contactSensor", title: "Which sensor?", required: true, multiple: false
	}
    section("Select Garage door...") {
		input name: "garageDoor", type: "capability.garageDoorControl", title: "Which door?", required: true, multiple: false
	}
	section("Check them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Warn with text message (optional)",
                description: "Phone Number", required: false
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "startTimerCallback")
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	schedule(startTime, "startTimerCallback")
}
def startTimerCallback() {
    if ("closed" != sensor.currentContact) {
    	if ("closed" == garageDoor.currentDoor) {
        	log.debug "Opening ${garageDoor.displayName}"
    		garageDoor.open()
            runIn(60, closeDoor)
        } else {
            closeDoor()
        }
   	} else {
    	log.debug "${sensor.displayName} is closed, no action will be taken."
    }
}

def closeDoor() {
	log.debug "Closing ${garageDoor.displayName}"
    garageDoor.close()
    
    runIn(60 * 2, checkDoor)
}

def checkDoor() {
	if ("closed" != sensor.currentContact) {
    	log.debug "checkDoor: The ${garageDoor.displayName} is still open. Closing now..."
	    garageDoor.close()
        sendMessage("Garage Door failed to close. Check it!");
    } else {
    	log.debug "checkDoor: The ${garageDoor.displayName} is closed."
    }
}

def sendMessage(message) {
	if (location.contactBookEnabled && recipients) {
        log.debug "contact book enabled!"
        sendNotificationToContacts(message, recipients)
    } else {
        log.debug "contact book not enabled"
        if (phone) {
            sendSms(phone, message)
        }
    }
}
