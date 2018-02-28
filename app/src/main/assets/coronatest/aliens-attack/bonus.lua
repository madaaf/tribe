local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local strings = require "strings"

local pendingShownBonus
local wonBonus

local listeners = {}

---------------------------------------------------------------------------------

local function removeShownBonus ()

	local group = pendingShownBonus
	if group then
		transition.cancel(group)
		transition.to(group, { x=-100, time=125, onComplete=function() 
	 		display.remove(group)
	 	end})
	end
end

local function showBonus (text, icon, useEventName)

	removeShownBonus()

	local text 		 = display.newText( text, 0, screenH/2 + 100, "assets/fonts/GULKAVE-REGULAR.ttf", 20 )
	local arrow 	 = display.newImageRect("assets/images/arrow.png", 9, 6)
	local background = display.newImageRect("assets/images/bg_bonus.png", 65, 65)
	local icon 	     = display.newImageRect(icon, 35, 36)
	
	arrow.y = text.y + 30
	background.y = arrow.y + 50
	icon.x, icon.y = background.x, background.y

	local group = display.newGroup()
	pendingShownBonus = group
	
 	group:insert(text)
 	group:insert(arrow)
 	group:insert(background)
 	group:insert(icon)
 	
 	icon.isHitTestable = true
 	background.isHitTestable = true

 	transition.to(group, { x=40, rotation=-3, time=125 })

	transition.to(group, { x=-100, time=125, delay=3000, onComplete=function() 
 		display.remove(group)
 	end})

 	group:addEventListener('touch', function (event)

 		if event.phase == "began" then

			removeShownBonus()

		 	if listeners[useEventName] then
		 		listeners[useEventName]()
			end
	 	end
 	end)
end

---------------------------------------------------------------------------------

local exports = {}

exports.addBonusListener = function(event, listener)
	listeners[event] = listener
end

exports.removeBonuses = function ()
	removeShownBonus()
end

exports.showBomb = function (params)
	log('showBomb')
	showBonus('ALL AT\nONCE!', 'assets/images/bomb.png', 'useBomb')
end

exports.showWatch = function (params)
	log('showWatch')
	showBonus('SLOW\nDOWN', 'assets/images/watch.png', 'useWatch')
end

return exports
