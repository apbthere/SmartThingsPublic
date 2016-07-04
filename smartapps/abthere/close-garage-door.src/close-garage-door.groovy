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
            log.debug "Will close ${garageDoor.displayName} in 60 seconds"
            garageDoor.close(delay:6000)
        } else {
            log.debug "Closing ${garageDoor.displayName}"
            garageDoor.close()
        }
   	} else {
    	log.debug "${sensor.displayName} is closed, no action will be taken."
    }
}
