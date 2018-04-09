
local strings = require 'strings'

local screenW, screenH = display.contentWidth, display.contentHeight

local moveJoystick, shootJoystick
local moveTutorial, shootTutorial

local exports = {}

exports.create = function ()
		
	local libAnalogStick = require 'joysticks.lib_analog_stick'

	moveJoystick = libAnalogStick.NewStick( 
		        {
		        x             = -screenW,
		        y             = screenH * 3/4,
		        thumbSize     = 80,
		        borderSize    = 60, 
		        snapBackSpeed = .2, 
		        joystickImage = 'assets/images/joystick.png',
		        joystickOverlay = 'assets/images/move.png'
		        } )

	shootJoystick = libAnalogStick.NewStick( 
		        {
		        x             = screenW * 2,
		        y             = screenH * 3/4,
		        thumbSize     = 80,
		        borderSize    = 60, 
		        snapBackSpeed = .2, 
		        joystickImage = 'assets/images/joystick.png',
		        joystickOverlay = 'assets/images/shoot.png'
		        } )

	local fontName = 'CorporativeSansRdAltBlack'
	local fontSize = 30

	moveTutorial = display.newText({ text=strings('move'), x=-screenW, y=screenH * 3/4 - 60, font=fontName, fontSize=fontSize })
	moveTutorial.rotation = -2

	shootTutorial = display.newText({ text=strings('shoot'), x=screenW * 2, y=screenH * 3/4 - 60, font=fontName, fontSize=fontSize })
	shootTutorial.rotation = -2
end

exports.hideTutorial = function ()
	transition.to(moveTutorial,  { x = -screenW,    time=500 })
	transition.to(shootTutorial, { x = screenW * 2, time=500 })
end

exports.showTutorial = function ()
	transition.to(moveTutorial,  { x = 80, 			 time=500 })
	transition.to(shootTutorial, { x = screenW - 80, time=500 })
end

exports.show = function ()
	transition.to(moveJoystick,  { x = 80, 			 time=500 })
	transition.to(shootJoystick, { x = screenW - 80, time=500 })
end

exports.hide = function ()
	transition.to(moveJoystick,  { x = -screenW,    time=500 })
	transition.to(shootJoystick, { x = screenW * 2, time=500 })
end

exports.getMoving = function ()
	return shootJoystick:getMoving()
end

exports.getAngleRad = function ()
	return shootJoystick:getAngleRad()
end

exports.move = function (target, speed)
	return moveJoystick:move(target, speed, false)
end

exports.rotate = function (target)
	return shootJoystick:rotate(target, true)
end

return exports
