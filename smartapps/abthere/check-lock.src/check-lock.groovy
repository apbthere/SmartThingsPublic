definition(
    name: "check lock",
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
    section("Check them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
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
    sendPush(message)
}

def startTimerCallback() {
	log.debug "Checking the locks"
    
	locks
    	.each {lock -> 
            sendMyMessage("The ${lock.displayName} is ${lock.currentLock}!")
    	}
}