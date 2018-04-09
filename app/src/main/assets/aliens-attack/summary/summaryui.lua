---------------------------------------------------------------------------------
-- Modules

local composer  = require "composer"
local widget    = require "widget"
local strings   = require "summary.strings"

---------------------------------------------------------------------------------
-- Parameters

local screenW, screenH          = display.actualContentWidth, display.actualContentHeight
local fontName                  = 'ProximaNovaSoft-Bold'
local summaryTitleSize          = 55
local summarySubitleSize        = 19
local summaryDefaultSize        = 15
local animationsDuration        = 300
local animationsDelay           = 50
local animationYDelta 		    = -35
local titleHeight               = 50
local spaceTitleSubtitle        = 5
local subtitleHeight            = 21
local spaceSubitlePlayAgain     = 40
local playAgainHeight 		    = 80
local spacePlayAgainLeaderboard = 10
local leaderboardHeight	        = 32
local spaceBottom               = 120 + animationYDelta

local exports                   = {}

---------------------------------------------------------------------------------
-- Local Functions

local function fadeInAndMoveUp(target, delay, params)
	transition.to(target, { y = animationYDelta, time = animationsDuration, delay = delay, transition = easing.outBack, delta = true, onComplete = params.onComplete })
	transition.fadeIn(target, { time = animationsDuration, delay = delay })
end

local function fadeOutAndMoveDown(target, delay, params)
    transition.to(target, { y = -animationYDelta, time = animationsDuration, delay = delay, transition = easing.outBack, delta = true, onComplete = params.onComplete })
    transition.fadeOut(target, { time = animationsDuration, delay = delay })
end

---------------------------------------------------------------------------------
-- Export Functions

exports.showTitle = function (score, sceneGroup, params)

    local font = composer.getVariable( "titleFontName" )
	local options = {
		id 			= "summaryTitle",
    	text 		= score,     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - leaderboardHeight - spacePlayAgainLeaderboard - playAgainHeight - spaceSubitlePlayAgain - subtitleHeight - spaceTitleSubtitle - titleHeight / 2,
    	width 		= screenW - 10 - 10,
    	height 		= titleHeight,
    	font 		= font,   
    	fontSize 	= summaryTitleSize,
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

exports.showSubtitle = function (bestScore, sceneGroup, params)

    local font = composer.getVariable( "titleFontName" )
    local options = {
        id          = "summarySubtitle",
        text        = ('' .. strings('summary_subtitle'):upper() .. bestScore),     
        x           = screenW / 2,
        y           = screenH - spaceBottom - leaderboardHeight - spacePlayAgainLeaderboard - playAgainHeight - spaceSubitlePlayAgain - subtitleHeight / 2,
        width       = screenW - 10 - 10,
        height      = subtitleHeight,
        font        = font,   
        fontSize    = summarySubitleSize,
        align       = "center"  -- Alignment parameter
    }

    local subtitleText = display.newText(options)
    sceneGroup:insert(subtitleText)
    subtitleText.alpha = 0

    fadeInAndMoveUp(subtitleText, animationsDelay, { onComplete=function () 
        -- Nothing to do...
    end })

    return subtitleText

end

exports.hideSubtitle = function (subtitle, params)
    fadeOutAndMoveDown(title, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

exports.showPlayAgainButton = function (sceneGroup, playAgainButtonEvent, params)

    local options = {
        frames =
        {
            { x=0, y=0, width=50, height=40 },
            { x=50, y=0, width=89, height=40 },
            { x=139, y=0, width=50, height=40 },
            { x=0, y=40, width=50, height=1 },
            { x=50, y=40, width=89, height=1 },
            { x=139, y=40, width=50, height=1 },
            { x=0, y=41, width=50, height=39 },
            { x=50, y=41, width=89, height=39 },
            { x=139, y=41, width=50, height=39 },
            { x=0, y=0, width=50, height=40 },
            { x=50, y=0, width=89, height=40 },
            { x=139, y=0, width=50, height=40 },
            { x=0, y=40, width=50, height=1 },
            { x=50, y=40, width=89, height=1 },
            { x=139, y=40, width=50, height=1 },
            { x=0, y=41, width=50, height=39 },
            { x=50, y=41, width=89, height=39 },
            { x=139, y=41, width=50, height=39 },
        },
        sheetContentWidth = 189,
        sheetContentHeight = playAgainHeight
    }
    local buttonSheet = graphics.newImageSheet( "summary/assets/images/summary_play_again_button_bg.png", options )

	local options = {
		id 			= "playAgainButton",
    	label		= strings('summary_play_again_button'),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - leaderboardHeight - spacePlayAgainLeaderboard - playAgainHeight / 2,
    	width 		= screenW - 10 - 10,
    	height 		= playAgainHeight,
    	font 		= fontName,   
    	fontSize 	= summaryDefaultSize,
    	align 		= "center",
    	labelColor 	= { default={ 0, 0, 0 }, over={ 0, 0, 0, 0.5 } },
        onEvent     = playAgainButtonEvent,
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
	local playAgainButton = widget.newButton(options)
	sceneGroup:insert(playAgainButton)
	playAgainButton.alpha = 0

    -- transition.scaleTo( facebookButton, { xScale=0, yScale=0, delay=50, time=300 } )
    playAgainButton.xScale = 0.1
    playAgainButton.yScale = 0.1
    transition.scaleTo(playAgainButton, { xScale=1.0, yScale=1.0, time=animationsDuration, delay=animationsDelay, transition = easing.outBack })
	fadeInAndMoveUp(playAgainButton, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return playAgainButton

end

exports.hidePlayAgainButton = function (playAgainButton, params)
    fadeOutAndMoveDown(playAgainButton, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

exports.showLeaderboardButton = function (sceneGroup, leaderboardButtonEvent, params)

    local options = {
        frames =
        {
            { x=0, y=0, width=30, height=16 },
            { x=30, y=0, width=167, height=16 },
            { x=197, y=0, width=30, height=16 },
            { x=0, y=16, width=30, height=1 },
            { x=30, y=16, width=167, height=1 },
            { x=197, y=16, width=30, height=1 },
            { x=0, y=17, width=30, height=15 },
            { x=30, y=17, width=167, height=15 },
            { x=197, y=17, width=30, height=15 },
            { x=0, y=0, width=30, height=16 },
            { x=30, y=0, width=167, height=16 },
            { x=197, y=0, width=30, height=16 },
            { x=0, y=16, width=30, height=1 },
            { x=30, y=16, width=167, height=1 },
            { x=197, y=16, width=30, height=1 },
            { x=0, y=17, width=30, height=15 },
            { x=30, y=17, width=167, height=15 },
            { x=197, y=17, width=30, height=15 },
        },
        sheetContentWidth = 227,
        sheetContentHeight = leaderboardHeight
    }
    local buttonSheet = graphics.newImageSheet( "summary/assets/images/summary_leaderboard_button_bg.png", options )

	local options = {
		id 			= "leaderboardButton",
    	label		= strings('summary_leaderboard_button'),     
    	x 			= screenW / 2,
    	y 			= screenH - spaceBottom - leaderboardHeight / 2,
    	width 		= 227,
    	height 		= leaderboardHeight,
    	font 		= fontName,   
    	fontSize 	= summaryDefaultSize,
    	align 		= "center",
    	labelColor 	= { default={ 1, 1, 1 }, over={ 1, 1, 1, 0.5 } },
        onEvent     = leaderboardButtonEvent,
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
	local leaderboardButton = widget.newButton(options)
	sceneGroup:insert(leaderboardButton)
	leaderboardButton.alpha = 0

	fadeInAndMoveUp(leaderboardButton, animationsDelay, { onComplete=function () 
 		-- Nothing to do...
 	end })

    return leaderboardButton

end

exports.hideLeaderboardButton = function (leaderboardButton, params)
    fadeOutAndMoveDown(leaderboardButton, 0, { onComplete=function () 
        -- Nothing to do...
    end })
end

return exports