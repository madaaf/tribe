local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local strings	= require "strings"

---------------------------------------------------------------------------------

local fontName = 'Gulkave Regular'

local title = strings('title')

local defaultSize = 20
local titleSize   = 40

---------------------------------------------------------------------------------

local function fadeOutAndMoveDown(target, params)
	
	transition.moveBy(target, { delay=1500, y=20, time=200 })
	transition.fadeOut(target, { delay=1500, time=150, onComplete=function()
		
		display.remove(target)

		if params and params.onComplete then
			params.onComplete()
		end
	end });
end

local function fadeInAndMoveUp(target, params)
	
	transition.moveTo(target, { x=screenW/2, y=screenH/2, time=200, onComplete=params.onComplete })
	transition.fadeIn(target, { time=150 });
end

local function showMessage (message, params) 
	log('showMessage - ' .. message)

	local text = display.newText( message:upper(), screenW/2, screenH/2 + 20, fontName, defaultSize )
 	text.alpha = 0

 	fadeInAndMoveUp(text, { onComplete=function ()
 		fadeOutAndMoveDown(text, params)
 	end})
end

---------------------------------------------------------------------------------

local exports = {}

exports.showTitle = function (params) 
	log('showTitle')

	local titleText = display.newText( title:upper(), screenW/2 + 20, screenH/2 + 20, fontName, titleSize )
 	titleText.alpha = 0

 	fadeInAndMoveUp(titleText, { onComplete=function () 
 		local shadowText = display.newText( title:upper(), screenW/2, screenH/2, fontName, titleSize )
 		shadowText.alpha = 0.5
		
		local offset = 1.5

 		transition.moveBy(titleText, { x=-offset, y=-offset, onComplete=function() fadeOutAndMoveDown(titleText, params) end })
 		transition.moveBy(shadowText, { x=offset, y=offset, onComplete=function() fadeOutAndMoveDown(shadowText) end })
 	end })
end

exports.showLetsGo = function (params)
	showMessage(strings('lets_go'), params)
end

exports.showYouLost = function (params)
	showMessage(strings('you_lost'), params)
end

exports.showYouWon = function (params)
	showMessage(strings('you_won'), params)
end

exports.showGameOver = function (params)
	showMessage(strings('game_over'), params)
end

exports.showSomeoneWon = function (name, params)
	showMessage(string.format(strings('someone_won'), name), params)
end

exports.showSomeoneLost = function (name, params)
	showMessage(string.format(strings('someone_lost'), name), params)
end

return exports
