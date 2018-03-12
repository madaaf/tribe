local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local widget    = require "widget"
local strings	= require "revive.strings"
local composer   = require "composer"

---------------------------------------------------------------------------------

local fontName = 'ProximaNovaSoft-Bold'

local reviveTitleSize   = 19
local reviveDefaultSize = 15

local animationsDuration 	= 300
local animationsDelay       = 50
local animationYDelta 		= -35
local titleHeight 			= 21
local spaceTitleMessage 	= 15
local messageHeight 		= 35
local spaceMessageFacebook 	= 10
local facebookHeight 		= 80
local spaceFacebookSkip 	= 10
local skipHeight	 		= 32
local spaceBottom 			= 120 + animationYDelta

local function fadeInAndMoveUp(target, delay, params)
	transition.to(target, { y = animationYDelta, time = animationsDuration, delay = delay, transition = easing.outBack, delta = true, onComplete = params.onComplete })
	transition.fadeIn(target, { time = animationsDuration, delay = delay })
end

local function fadeOutAndMoveDown(target, delay, params)
    transition.to(target, { y = -animationYDelta, time = animationsDuration, delay = delay, transition = easing.outBack, delta = true, onComplete = params.onComplete })
    transition.fadeOut(target, { time = animationsDuration, delay = delay })
end

local exports = {}

exports.showTitle = function (sceneGroup, params)

    local font = composer.getVariable( "titleFontName" )
	local options = {
		id 			= "reviveTitle",
    	text 		= strings('revive_title'):upper(),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - skipHeight - spaceFacebookSkip - facebookHeight - spaceMessageFacebook - messageHeight - spaceTitleMessage - titleHeight / 2,
    	width 		= screenW - 10 - 10,
    	height 		= titleHeight,
    	font 		= font,   
    	fontSize 	= reviveTitleSize,
    	align 		= "center"  -- Alignment parameter
	}

	local titleText = display.newText(options)
	sceneGroup:insert(titleText)
 	titleText.alpha = 0

 	fadeInAndMoveUp(titleText, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return titleText

end

exports.hideTitle = function (title, params)
    fadeOutAndMoveDown(title, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

exports.showMessage = function (sceneGroup, params)
	
	local options = {
		id 			= "reviveMessage",
    	text 		= strings('revive_message'),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - skipHeight - spaceFacebookSkip - facebookHeight - spaceMessageFacebook - messageHeight / 2,
    	width 		= screenW - 10 - 10,
    	height 		= messageHeight,
    	font 		= fontName,   
    	fontSize 	= reviveDefaultSize,
    	align 		= "center"  -- Alignment parameter
	}
 
	local messageText = display.newText(options)
    messageText:setFillColor( 1, 1, 1, 0.4 )
	sceneGroup:insert(messageText)
	messageText.alpha = 0

 	fadeInAndMoveUp(messageText, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return messageText

end

exports.hideMessage = function (message, params)
    fadeOutAndMoveDown(message, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

exports.showFacebookButton = function (sceneGroup, facebookButtonEvent, params)

    local options = {
        frames =
        {
            { x=0, y=0, width=50, height=40 },
            { x=50, y=0, width=180, height=40 },
            { x=230, y=0, width=50, height=40 },
            { x=0, y=40, width=50, height=1 },
            { x=50, y=40, width=180, height=1 },
            { x=230, y=40, width=50, height=1 },
            { x=0, y=41, width=50, height=39 },
            { x=50, y=41, width=180, height=39 },
            { x=230, y=41, width=50, height=39 },
            { x=0, y=0, width=50, height=40 },
            { x=50, y=0, width=180, height=40 },
            { x=230, y=0, width=50, height=40 },
            { x=0, y=40, width=50, height=1 },
            { x=50, y=40, width=180, height=1 },
            { x=230, y=40, width=50, height=1 },
            { x=0, y=41, width=50, height=39 },
            { x=50, y=41, width=180, height=39 },
            { x=230, y=41, width=50, height=39 },
        },
        sheetContentWidth = 280,
        sheetContentHeight = facebookHeight
    }
    local buttonSheet = graphics.newImageSheet( "revive/assets/images/revive_facebook_button_bg.png", options )

	local options = {
		id 			= "facebookButton",
    	label		= strings('revive_facebook_button'),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - skipHeight - spaceFacebookSkip - facebookHeight / 2,
    	width 		= screenW - 10 - 10,
    	height 		= facebookHeight,
    	font 		= fontName,   
    	fontSize 	= reviveDefaultSize,
    	align 		= "center",
    	labelColor 	= { default={ 1, 1, 1 }, over={ 1, 1, 1, 0.5 } },
        onEvent     = facebookButtonEvent,
    	-- shape		= "roundedRect",
    	-- fillColor 	= { default = { 68/255, 96/255, 160/255, 1 }, over = { 68/255, 96/255, 160/255, 0.5 } },
        -- defaultFile = "revive/assets/images/revive_facebook_button_bg.png",
        sheet                   = buttonSheet,
        topLeftFrame            = 1,
        topMiddleFrame          = 2,
        topRightFrame           = 3,
        middleLeftFrame         = 4,
        middleFrame             = 5,
        middleRightFrame        = 6,
        bottomLeftFrame         = 7,
        bottomMiddleFrame       = 8,
        bottomRightFrame        = 9,
        topLeftOverFrame        = 10,
        topMiddleOverFrame      = 11,
        topRightOverFrame       = 12,
        middleLeftOverFrame     = 13,
        middleOverFrame         = 14,
        middleRightOverFrame    = 15,
        bottomLeftOverFrame     = 16,
        bottomMiddleOverFrame   = 17,
        bottomRightOverFrame    = 18,
	}
	
	-- This will create a button using text
	local facebookButton = widget.newButton(options)
	sceneGroup:insert(facebookButton)
	facebookButton.alpha = 0

    -- transition.scaleTo( facebookButton, { xScale=0, yScale=0, delay=50, time=300 } )
    facebookButton.xScale = 0.1
    facebookButton.yScale = 0.1
    transition.scaleTo(facebookButton, { xScale=1.0, yScale=1.0, time=animationsDuration, delay=animationsDelay, transition = easing.outBack })
	fadeInAndMoveUp(facebookButton, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return facebookButton

end

exports.hideFacebookButton = function (facebookButton, params)
    fadeOutAndMoveDown(facebookButton, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

exports.showSkipButton = function (sceneGroup, skipButtonEvent, params)

    local options = {
        frames =
        {
            { x=0, y=0, width=30, height=16 },
            { x=30, y=0, width=68, height=16 },
            { x=98, y=0, width=30, height=16 },
            { x=0, y=16, width=30, height=1 },
            { x=30, y=16, width=68, height=1 },
            { x=98, y=16, width=30, height=1 },
            { x=0, y=17, width=30, height=15 },
            { x=30, y=17, width=68, height=15 },
            { x=98, y=17, width=30, height=15 },
            { x=0, y=0, width=30, height=16 },
            { x=30, y=0, width=68, height=16 },
            { x=98, y=0, width=30, height=16 },
            { x=0, y=16, width=30, height=1 },
            { x=30, y=16, width=68, height=1 },
            { x=98, y=16, width=30, height=1 },
            { x=0, y=17, width=30, height=15 },
            { x=30, y=17, width=68, height=15 },
            { x=98, y=17, width=30, height=15 },
        },
        sheetContentWidth = 128,
        sheetContentHeight = skipHeight
    }
    local buttonSheet = graphics.newImageSheet( "revive/assets/images/revive_skip_button_bg.png", options )

	local options = {
		id 			= "skipButton",
    	label		= strings('revive_skip_button'),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - skipHeight / 2,
    	width 		= 168,
    	height 		= skipHeight,
    	font 		= fontName,   
    	fontSize 	= reviveDefaultSize,
    	align 		= "center",
    	labelColor 	= { default={ 1, 1, 1 }, over={ 1, 1, 1, 0.5 } },
        onEvent     = skipButtonEvent,
    	-- shape		= "roundedRect",
    	-- fillColor 	= { default = { 0, 0, 0, 0 }, over = { 0, 0, 0, 0 } },
        sheet                   = buttonSheet,
        topLeftFrame            = 1,
        topMiddleFrame          = 2,
        topRightFrame           = 3,
        middleLeftFrame         = 4,
        middleFrame             = 5,
        middleRightFrame        = 6,
        bottomLeftFrame         = 7,
        bottomMiddleFrame       = 8,
        bottomRightFrame        = 9,
        topLeftOverFrame        = 10,
        topMiddleOverFrame      = 11,
        topRightOverFrame       = 12,
        middleLeftOverFrame     = 13,
        middleOverFrame         = 14,
        middleRightOverFrame    = 15,
        bottomLeftOverFrame     = 16,
        bottomMiddleOverFrame   = 17,
        bottomRightOverFrame    = 18,
	}
	
	-- This will create a button using text
	local skipButton = widget.newButton(options)
	sceneGroup:insert(skipButton)
	skipButton.alpha = 0

	fadeInAndMoveUp(skipButton, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return skipButton

end

exports.hideSkipButton = function (skipButton, params)
    fadeOutAndMoveDown(skipButton, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

return exports