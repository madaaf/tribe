---------------------------------------------------------------------------------
-- Modules

local composer  = require "composer"
local summaryui	= require "summary.summaryui"
local strings 	= require "summary.strings"

---------------------------------------------------------------------------------
-- Parameters

local title
local subtitle
local playAgainButton
local leaderboardButton
local capture
local countdownTimer

local scene                 = composer.newScene()
local animationsDuration    = 300

---------------------------------------------------------------------------------
-- Scene

function scene:create(event)
    log('summary - scene:create')

    capture = display.captureScreen()
    capture.x, capture.y = display.contentCenterX, display.contentCenterY
    capture.fill.effect = "filter.blurGaussian"
    capture.fill.effect.horizontal.blurSize = 100
    capture.fill.effect.vertical.blurSize = 100

    local halfWidth = display.contentWidth / 2
    local halfHeight = display.contentHeight / 2
    local background = display.newRect(
        display.screenOriginX + halfWidth, 
        display.screenOriginY + halfHeight, 
        halfWidth*2, 
        halfHeight*2
    )
    background:setFillColor(0, 0, 0, 0.5)

    local sceneGroup = self.view
    sceneGroup:insert(capture)
    sceneGroup:insert(background)

end

function scene:show(event)
    log('summary - scene:show' .. event.phase)

    if event.phase == "will" then

        local parent = event.parent
        local sceneGroup = self.view

        tilte = summaryui.showTitle(255, sceneGroup)
		subtilte = summaryui.showSubtitle(0, sceneGroup)
        
        -- Play Again

        local playAgainButtonEvent = function (clickEvent)
            if clickEvent.phase == "ended" then
            	parent:playAgain()
                composer.hideOverlay( "fade", animationsDuration )
            end
        end
        playAgainButton = summaryui.showPlayAgainButton(sceneGroup, playAgainButtonEvent)


        local remaningTime = 5
        local updateCountdown = function()
        	if remaningTime <= 0 then
		    	if countdownTimer then
		    		timer.cancel(countdownTimer)
		    	end
        		parent:playAgain()
                composer.hideOverlay( "fade", animationsDuration )
        	else
	        	playAgainButton:setLabel((strings('summary_play_again_button') .. remaningTime):upper())
	        	remaningTime = remaningTime - 1
        	end
    	end
    	updateCountdown()
        countdownTimer = timer.performWithDelay( 1000, updateCountdown, 5 )

        -- Leaderboard

        local leaderboardButtonEvent = function (clickEvent)
            if clickEvent.phase == "ended" then
            	parent:showLeaderboard()
                composer.hideOverlay( "fade", animationsDuration )
            end
        end
        leaderboardButton = summaryui.showLeaderboardButton(sceneGroup, leaderboardButtonEvent)

    end

end

function scene:hide(event)
    log('summary - scene:hide')

    if event.phase == "will" then

    	if countdownTimer then
    		timer.cancel(countdownTimer)
    	end

        summaryui.hideTitle(title, { onComplete=function () 
            -- Nothing to do...
        end })

		summaryui.hideSubtitle(subtilte, { onComplete=function () 
            -- Nothing to do...
        end })

        summaryui.hidePlayAgainButton(playAgainButton, { onComplete=function () 
            -- Nothing to do...
        end })

        summaryui.hideLeaderboardButton(leaderboardButton, { onComplete=function () 
            -- Nothing to do...
        end })
        
    end
end

function scene:destroy(event)
    log('summary - scene:destroy')
end

scene:addEventListener('create',  scene)
scene:addEventListener('show',  scene)
scene:addEventListener('hide',  scene)
scene:addEventListener('destroy', scene)

return scene