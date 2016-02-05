definition(
    name: "Once a Day Security check",
    namespace: "abthere",
    author: "abthere",
    description: "Check the locks/doors and garage doors at a specified time and send notification if any are unlocked.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences { 
	section("Select locks to check...") {
		input name: "locks", type: "capability.lock", title: "Which lock?", required: false, multiple: true
	}
    section("Select sensors to check...") {
		input name: "sensors", type: "capability.contactSensor", title: "Which sensor?", required: false, multiple: true
	}
    section("Select doors to check...") {
		input name: "doorSensors", type: "capability.doorControl", title: "Which door?", required: false, multiple: true
	}
	section("Check them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
    section("Then flash if still open..."){
		input "switches", "capability.switch", title: "These lights", multiple: true
		input "numFlashes", "number", title: "This number of times (default 3)", required: false
	}
	section("Time settings in milliseconds (optional)..."){
		input "onFor", "number", title: "On for (default 1000)", required: false
		input "offFor", "number", title: "Off for (default 1000)", required: false
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
	unschedule()
	schedule(startTime, "startTimerCallback")
}

def sendMyMessage(message) {
	log.debug message
    if (location.contactBookEnabled && recipients) {
        log.debug "contact book enabled!"
        sendNotificationToContacts(message, recipients)
    } else {
        log.debug "contact book not enabled"
        if (phone) {
            sendSms(phone, message)
        }
    }
    sendPush(message)
}

def startTimerCallback() {
	log.debug "Checking the locks"
    
    def shouldFlash = false
    
	locks.findAll {lock -> "locked" != lock.currentLock}
    	.each {lock -> 
            sendMyMessage("The ${lock.displayName} is ${lock.currentLock}!")
            shouldFlash = true
    	}
    
    sensors.findAll {sensor -> "closed" != sensor.currentContact}
    	.each {sensor -> 
            sendMyMessage("The ${sensor.displayName} is ${sensor.currentContact}!")
            shouldFlash = true
    	}
    
    doorSensors.findAll {door -> "closed" != door.currentDoor}
    	.each {door -> 
            sendMyMessage("The ${door.displayName} is ${door.currentDoor}!")
            shouldFlash = true
    	}
    
    if (shouldFlash) {
    	flashLights()
    }
}
    
private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect{it.currentSwitch != "on"}
		def delay = 0L

        numFlashes.times {
            log.trace "Switch on after  $delay msec"
            switches.eachWithIndex {s, i ->
                if (initialActionOn[i]) {
                    s.on(delay: delay)
                }
                else {
                    s.off(delay:delay)
                }
            }
            delay += onFor
            log.trace "Switch off after $delay msec"
            switches.eachWithIndex {s, i ->
                if (initialActionOn[i]) {
                    s.off(delay: delay)
                }
                else {
                    s.on(delay:delay)
                }
            }
            delay += offFor
        }
   }
}