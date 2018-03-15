-- Lua

local composer  = require "composer"

-- Local

local reviveui  = require "revive.reviveui"
local strings   = require "revive.strings"

local animationsDuration    = 300

---------------------------------------------------------------------------------

local scene = composer.newScene()
local capture

---------------------------------------------------------------------------------

function scene:create(event)
    log('revive - scene:create')

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

local tilte
local message
local facebookButton
local skipButton

function scene:show(event)
    log('revive - scene:show' .. event.phase)

    if event.phase == "will" then

        local parent = event.parent
        local sceneGroup = self.view
        tilte = reviveui.showTitle(sceneGroup)
        message = reviveui.showMessage(sceneGroup)

        -- Facebook

        local facebookButtonEvent = function (clickEvent)
            if clickEvent.phase == "ended" then
                skipButton:setEnabled( false )
                facebookButton:setEnabled( false )
                facebookButton:setLabel( strings('revive_notifying') )

                if isSimulator then
                    timer.performWithDelay(1000, function() 
                        parent:doRevive()
                        composer.hideOverlay( "fade", animationsDuration )
                    end)
                else 
                    Runtime:dispatchEvent({ name = 'coronaView', event = 'revive' })
                end
                
            end
        end
        facebookButton = reviveui.showFacebookButton(sceneGroup, facebookButtonEvent)

        -- Skip

        local skipButtonEvent = function (clickEvent)
            if clickEvent.phase == "ended" then
                parent:cancelRevive()
                composer.hideOverlay( "fade", animationsDuration )
            end
        end
        skipButton = reviveui.showSkipButton(sceneGroup, skipButtonEvent)

        -- Revive went well

        local function shareToReviveSuccess(event)
            parent:doRevive()
            composer.hideOverlay( "fade", animationsDuration )
        end
        Runtime:addEventListener('shareToReviveSuccess', shareToReviveSuccess)


        -- Revive failed

        local function shareToReviveError(event) 
            parent:cancelRevive()
            composer.hideOverlay( "fade", animationsDuration )
        end
        Runtime:addEventListener('shareToReviveError', shareToReviveError)

    end

end

function scene:hide(event)
    log('revive - scene:hide')

    if event.phase == "will" then

        reviveui.hideTitle(title, { onComplete=function () 
            -- Nothing to do...
        end })

        reviveui.hideMessage(message, { onComplete=function () 
            -- Nothing to do...
        end })

        reviveui.hideFacebookButton(facebookButton, { onComplete=function () 
            -- Nothing to do...
        end })

        reviveui.hideSkipButton(skipButton, { onComplete=function () 
            -- Nothing to do...
        end })
        
    end
end

function scene:destroy(event)
    log('revive - scene:destroy')
end

scene:addEventListener('create',  scene)
scene:addEventListener('show',  scene)
scene:addEventListener('hide',  scene)
scene:addEventListener('destroy', scene)

return scene