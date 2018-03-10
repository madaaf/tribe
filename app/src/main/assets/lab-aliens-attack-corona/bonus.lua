local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local strings = require "strings"
local emitter = require "emitter"

local pendingShownBomb
local pendingShownWatch

local listeners = {}

---------------------------------------------------------------------------------

local function removeShownBonuses (group)

	if group then
		transition.cancel(group)
		transition.to(group, { x=-100, time=125, onComplete=function() 
	 		display.remove(group)
	 	end})
	end
end

local function removeShownBomb ()
	removeShownBonuses(pendingShownBomb)
	pendingShownBomb = nil
end

local function removeShownWatch ()
	removeShownBonuses(pendingShownWatch)
	pendingShownWatch = nil
end

local function moveExistingBonusTop (group)
	if group then
		transition.to(group, { y=-100, time=125 })
		if group[2] then group[2].isVisible = false end
		if group[1] then group[1].isVisible = false end
	end
end

local function moveExistingBonusBottom (group)
	if group then
		transition.to(group, { y=0, time=125 })
	end
end

local function showBonus (text, icon, useEventName, otherBonusGroup)

	local text 		 = display.newText( text, 0, screenH - 150 - 50 - 30, 'Gulkave Regular', 20 )
	local arrow 	 = display.newImageRect("assets/images/arrow.png", 9, 6)
	local background = emitter.newBonusEmitter()
	local icon 	     = display.newImageRect(icon, 44, 44)
	
	arrow.y = text.y + 30
	background.y = arrow.y + 50
	icon.x, icon.y = background.x, background.y

	local group = display.newGroup()
	pendingShownBonus = group

 	group:insert(text)
 	group:insert(arrow)
 	group:insert(background)
 	group:insert(icon)
 	
	if otherBonusGroup then
		text.isVisible  = false
		arrow.isVisible = false
	end

 	icon.isHitTestable = true
 	background.isHitTestable = true

 	transition.to(group, { x=40, rotation=-3, time=125 })

 	group:addEventListener('touch', function (event)

 		if event.phase == "began" then

			removeShownBonuses(group)
			moveExistingBonusBottom(otherBonusGroup)

		 	if listeners[useEventName] then
		 		listeners[useEventName]()
			end
	 	end
 	end)

 	return group
end

---------------------------------------------------------------------------------

local exports = {}

exports.addBonusListener = function(event, listener)
	listeners[event] = listener
end

exports.removeBonuses = function ()
	removeShownBomb()
	removeShownWatch()
end

exports.showBomb = function (params)
	log('showBomb')
	removeShownBomb()
	moveExistingBonusTop(pendingShownWatch)
	pendingShownBomb = showBonus('ALL AT\nONCE!', 'assets/images/bomb.png', 'useBomb', pendingShownWatch)
end

exports.showWatch = function (params)
	log('showWatch')
	removeShownWatch()
	moveExistingBonusTop(pendingShownBomb)
	pendingShownWatch = showBonus('SLOW\nDOWN', 'assets/images/watch.png', 'useWatch', pendingShownBomb)
end

return exports
