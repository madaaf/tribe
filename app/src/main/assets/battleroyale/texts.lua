---------------------------------------------------------------------------------
-- Modules

local strings	= require "strings"

---------------------------------------------------------------------------------
-- Parameters

local screenW, screenH 	= display.actualContentWidth, display.actualContentHeight
local fontName 			= 'CorporativeSansRdAltBlack'
local defaultSize 		= 25
local exports 			= {}

local verticalOffset = 0

---------------------------------------------------------------------------------
-- Local Functions

local function fadeOutAndMoveDown(target, params)
	
	transition.moveBy(target, { delay=1500, y=20, time=200 })
	transition.fadeOut(target, { delay=1500, time=150, onComplete=function()
		
		display.remove(target)

		verticalOffset = verticalOffset - target.height

		if params and params.onComplete then
			params.onComplete()
		end
	end });

end

local function fadeInAndMoveUp(target, params, alpha)
	
	verticalOffset = verticalOffset + target.height

	transition.moveBy(target, { y=-20, time=200, onComplete=params.onComplete })
	transition.to(target, { alpha=alpha, time=150 });
end

local function showMessage (message, params) 
	
	local text = display.newText( message, screenW/2, (screenH/3 + 20) + verticalOffset, fontName, defaultSize )
	text.align = 'center'
 	text.alpha = 0

 	fadeInAndMoveUp(text, { onComplete=function ()
 		fadeOutAndMoveDown(text, params)
 	end}, 1)
end

local currentInstructions
local currentMode
local currentModeBg
local connectionIssue

local function hideCurrentInstructions()
	if currentInstructions then
		verticalOffset = verticalOffset - currentInstructions.height
		display.remove(currentInstructions)
		currentInstructions = nil
	end
end

local function hideCurrentMode()
	if currentMode then
		display.remove(currentMode)
		currentMode = nil
	end
	if currentModeBg then
		display.remove(currentModeBg)
		currentModeBg = nil
	end
end

local function showInstructions (message) 

	hideCurrentInstructions()

	currentInstructions = display.newText({ text=message, x=screenW/2, y=(screenH/3 + 20) + verticalOffset, font=fontName, fontSize=30, align='center' })
 	currentInstructions.alpha = 0
	currentInstructions.rotation = -2

 	fadeInAndMoveUp(currentInstructions, {}, 0.1)
end

local function showMode(message)
	
	hideCurrentMode()

	currentMode = display.newText({ text=message, x=screenW/2, y=screenH*2/3 - 20, font=fontName, fontSize=15, align='center' })
 	currentMode.alpha = 0
	
	currentModeBg = display.newRoundedRect(currentMode.x, currentMode.y, currentMode.width + 20, currentMode.height + 20, 5)
 	currentModeBg:setFillColor(1, 1, 1, 0.1)
 	currentModeBg.alpha = 0
 	currentMode:toFront()

 	transition.to(currentMode, { alpha = 0.75 })
 	transition.to(currentModeBg, { alpha = 0.75 })
end

local function showBaseline (message, y, params) 
	local text = display.newText({ text=message, x=screenW/2, y=y + verticalOffset, font=fontName, fontSize=defaultSize, align='center'})
	text.align = 'center'
 	text.alpha = 0
 	text.rotation = -2

 	fadeInAndMoveUp(text, { onComplete=function ()
 		fadeOutAndMoveDown(text, params)
 	end}, 1)
end

local function moveToCenterAndOut(target, params)
	local previousX = target.x;
	transition.moveTo(target, { x=screenW/2, time=750, delay=1000, transition=easing.outQuart, 
		onComplete = function()
			transition.moveTo(target, { x=previousX, time=750, delay=1000, transition=easing.outQuart, 
				onComplete = function() 
					display.remove(target)
					if params and params.onComplete then
						params.onComplete()
					end
				end
			})
		end
	})
end

---------------------------------------------------------------------------------
-- Export Functions

exports.showConnectionIssue = function ()
	exports.hideConnectionIssue()

	connectionIssue = display.newText({ text=strings('connection_issue'), x=screenW/2, y=(screenH/3 + 20) + verticalOffset, font=fontName, fontSize=30, align='center' })
 	connectionIssue.alpha = 0
	connectionIssue.rotation = -2

 	fadeInAndMoveUp(connectionIssue, {}, 0.1)
end

exports.hideConnectionIssue = function ()
	if connectionIssue then
		verticalOffset = verticalOffset - connectionIssue.height
		display.remove(connectionIssue)
		connectionIssue = nil
	end
end

exports.showTitle = function (onComplete) 
	local topTitle = display.newImageRect('assets/images/logo_top.png', 138, 59)
	topTitle.x = -display.contentWidth
	topTitle.y = display.contentCenterY * 2/3 - 17

	local bottomTitle = display.newImageRect('assets/images/logo_bottom.png', 149, 67)
	bottomTitle.x = display.contentWidth + bottomTitle.width
	bottomTitle.y = display.contentCenterY * 2/3 + 17

	timer.performWithDelay(1250, function() showBaseline(strings('baseline'), bottomTitle.y + 100, { onComplete = onComplete }) end)

	moveToCenterAndOut(topTitle, {})
	moveToCenterAndOut(bottomTitle, {})
end

exports.showYouLost = function (params)
	showMessage(strings('you_lost'), params)
end

exports.showSomeoneLost = function (name, params)
	showMessage(string.format(strings('someone_lost'), name), params)
end

exports.showYouWon = function ()
	showMessage(strings('you_won'))
end

exports.showSomeoneWon = function (name)
	showMessage(string.format(strings('someone_won'), name))
end

exports.showGameOver = function ()
	showMessage(strings('game_over'))
end

exports.showWaitingInstructions = function ()
	showInstructions(strings('waiting_instructions'))
	showMode(strings('training_mode'))
end

exports.showPendingInstructions = function ()
	showInstructions(strings('pending_instructions'))
	showMode(strings('viewer_mode'))
end

exports.showStartingInstructions = function ()
	
	hideCurrentMode()
	hideCurrentInstructions()

	local text = string.format(strings('starting_instructions'), '3')

	local instructions = display.newText({ text=text, x=screenW/2, y=screenH, font=fontName, fontSize=30, align='center' })
 	instructions.anchorY = 0
 	instructions.rotation = -2

 	transition.to(instructions, { y = screenH - 180 })

 	timer.performWithDelay(900, function ()
 		instructions.text = string.format(strings('starting_instructions'), '2')
 	end)
 	timer.performWithDelay(1800, function ()
 		instructions.text = string.format(strings('starting_instructions'), '1')
 	end)
 	timer.performWithDelay(2700, function ()
 		transition.to(instructions, { y = screenH, onComplete = function ()
 			display.remove(instructions)
	 	end})
 	end)
end

exports.hideInstructions = function () 
	hideCurrentInstructions()
	hideCurrentMode()
end

return exports
