local screenW, screenH = display.actualContentWidth, display.actualContentHeight

local group
local ground, topGradient

local listeners = {}

local gameEnded = false

local EVENT_ALIEN_KILLED				= 'alienKilled'
local EVENT_ALIEN_WILL_REACH_THE_GROUND	= 'alienWillReachTheGround'
local EVENT_ALIEN_DID_REACH_THE_GROUND 	= 'alienDidReachTheGround'

local model   = require 'model'
local emitter = require 'emitter'

local groundHeight = 90

---------------------------------------------------------------------------------

local function loadGround()
	log('loadGround')

	ground = display.newRect(0, screenH, screenW, groundHeight)
	ground.anchorX = 0
	ground.anchorY = 1
	ground:setFillColor(0,0,0,0)

	group:insert(ground)
end

local function loadTopGradient()
	log('loadTopGradient')

	topGradient = display.newImageRect("assets/images/top_gradient.png", screenW, screenW/2)
	topGradient.anchorX = 0 
	topGradient.anchorY = 0

	group:insert(topGradient)
end

local function scaleDownAndRemoveAlien(alienGroup) 

	transition.cancel(alienGroup)

	local params = { xScale=0.1, yScale=0.1, alpha=0, time=125, onComplete=function() display.remove(alienGroup) end }
	transition.to(alienGroup, params)
end

local function showPointsAndRemoveAlien(alienGroup, points) 

	alienGroup.alien.isKilled = true

	local alienImage = alienGroup[2]
	local alienGradient = alienGroup[1]

	transition.cancel(alienGroup)

	local boomEmitter = emitter.newBoomEmitter(alienGroup)
	boomEmitter.x, boomEmitter.y = boomEmitter.x, boomEmitter.y + 10
	alienGroup:insert(boomEmitter)

	local pointsText = display.newText("" .. points, screenW/2, screenH/2 + 20, 'Circular', 30 )
	pointsText.alpha = 0
	pointsText.xScale, pointsText.yScale = 0.1,0.1
	pointsText.x, pointsText.y = alienImage.x, alienImage.y + 10
	alienGroup:insert(pointsText)
	transition.to(pointsText, { xScale=1, yScale=1, alpha=1, time=125, onComplete=function()
		transition.to(pointsText, { xScale=0.1, yScale=0.1, alpha=0, time=125, delay=200, onComplete=function () 
 			display.remove(alienGroup)
 		end })
	end })

	local params = { xScale=0.1, yScale=0.1, alpha=0, time=125 }
	transition.to(alienImage, params)
	transition.to(alienGradient, params)
end

local function onTouchAlien(event)

	if not gameEnded and event.phase == "began" then
		local alienGroup = event.target
		local alien = alienGroup.alien

		alienGroup.taps = alienGroup.taps + 1

		if alienGroup.taps >= alienGroup.tapsToKill then

			if alien.isKilled == false then

				local points = alienGroup.tapsToKill

				-- 0.9 because we double the points in the last 10%.
				if event.y > (((screenH - groundHeight) * 0.9) - alienGroup.height/2) then
					points = points * 2
				end

				showPointsAndRemoveAlien(alienGroup, points)

				if listeners[EVENT_ALIEN_KILLED] then
					listeners[EVENT_ALIEN_KILLED](points)
				end
				
			end

		else
			local alienImage = alienGroup[2]
			local alienImageHalfKilled = display.newImageRect("assets/images/alien_" .. alien.type .. "_half_killed.png", alienImage.width, alienImage.height)
			if alienImageHalfKilled then

				transition.to(alienGroup, { rotation=10, time=200 })

				alienGroup:remove(alienImage)
				alienGroup:insert(2, alienImageHalfKilled)
			end
		end
	end
end

local function alienWillCollide(alienGroup)

	local alien = alienGroup.alien
	local alienImage = alienGroup[2]
	
	if alienImage and alien.isKilled == false and alien.didCollide == false then

		if gameEnded then
			alienDidCollide(alienGroup)
		else 

			if listeners[EVENT_ALIEN_WILL_REACH_THE_GROUND] then
				listeners[EVENT_ALIEN_WILL_REACH_THE_GROUND](alienGroup)
			else 
				alienDidCollide(alienGroup)
			end
			
		end

	end
end

local function alienDidCollide(alienGroup)

	local alien = alienGroup.alien
	local alienImage = alienGroup[2]
	alien.didCollide = true

	if alienImage then

		local alienGradient = alienGroup[1]
		local alienImageLost = display.newImageRect("assets/images/alien_" .. alien.type .. "_lost.png", alienImage.width, alienImage.height)
		
		alienGroup:remove(alienImage)
		alienGroup:remove(alienGradient)

		alienGroup:insert(alienImageLost)

		if gameEnded then
			scaleDownAndRemoveAlien(alienGroup)

		else
			if listeners[EVENT_ALIEN_DID_REACH_THE_GROUND] then
				listeners[EVENT_ALIEN_DID_REACH_THE_GROUND]()
			end
		end
	end
end


---------------------------------------------------------------------------------

local aliens = {}

aliens.addAlienListener = function(event, listener)
	listeners[event] = listener
end

aliens.load = function()

	group = display.newGroup()

	-- loadGround()
	loadTopGradient()

	return group
end

local alienSourceY = -groundHeight
local alienTargetY = screenH - groundHeight

aliens.pop = function(alien, paceFactor)
	log('pop')
	
	local alienGroup, alienImage, alienGradient, alienZone

	if alien.type == 3 then
		alienImage = display.newImageRect( "assets/images/alien_3.png", 88, 34 )
	elseif alien.type == 1 then
		alienImage = display.newImageRect( "assets/images/alien_1.png", 51, 37 )
	elseif alien.type == 2 then
		alienImage = display.newImageRect( "assets/images/alien_2.png", 60, 39 )
	else 
		alienImage = display.newImageRect( "assets/images/alien_0.png", 48, 27 )
	end

	alienGradient = emitter.newAlienFallingEmitter()

	alienZone = display.newRect(0, 0, alienImage.width * 1.5, alienImage.height * 1.5)
	alienZone:setFillColor(1, 0, 0, 0)
 	alienZone.isHitTestable = true

	local alienGroup = display.newGroup()
	alienGroup.alien = alien
	alienGroup.startTime = os.time()

	alienGroup.taps = 0
	if not alien.taps then
		alienGroup.tapsToKill = 1
	else
		alienGroup.tapsToKill = alien.taps
	end

	alienGroup:scale(alien.scale, alien.scale)

	alienGroup:insert(alienGradient)
	alienGroup:insert(alienImage)
	alienGroup:insert(alienZone)

	alienGroup.x, alienGroup.y = (0.1 + (screenW * 0.85)) * alien.start, alienSourceY
	alienGradient.y = -10
	alienGroup.rotation = alien.rotation

	local alienShape = { 
		-alienImage.width/2, -alienImage.height/2, 
		 alienImage.width/2, -alienImage.height/2, 
		 alienImage.width/2,  alienImage.height/2, 
		-alienImage.width/2,  alienImage.height/2
	}

	local time = alien.speed * 1000 * paceFactor
	transition.cancel(alienGroup)
	local handleAlienCollision = function () alienWillCollide(alienGroup) end
	transition.to(alienGroup, { y=alienTargetY - alienImage.height/2, time=time, onComplete=handleAlienCollision })

	alienGroup:addEventListener('touch', onTouchAlien)

	group:insert(alienGroup)
	group:insert(topGradient) -- Always on top
end

aliens.changeAliensSpeed = function(paceFactor)
	log('changeAliensSpeed - ' .. paceFactor)
	
	for i=1,group.numChildren do

		if group[i].alien then
			local alienGroup = group[i]
			local alienImage = alienGroup[1]

			local remainingPercent = 1 - (alienGroup.y / (alienTargetY - alienSourceY))
			local remainingTime = remainingPercent * alienGroup.alien.speed * 1000 * paceFactor

			log('remainingPercent = ' .. remainingPercent)
			log('remainingTime = ' .. remainingTime)

			transition.cancel(alienGroup)

			if group[i].alien.didCollide == false then
				local handleAlienCollision = function () 
					if gameEnded then
						alienDidCollide(alienGroup)
					else 
						alienWillCollide(alienGroup) 
					end
				end
				transition.to(alienGroup, { y=alienTargetY - alienImage.height/2, time=remainingTime, onComplete=handleAlienCollision })
			end
		end
	end
end

aliens.killAliens = function()
	log('killAliens')
	
	local total = 0

	for i=1,group.numChildren do

		if group[i].alien then
			local alienGroup = group[i]
			total = total + alienGroup.tapsToKill
			showPointsAndRemoveAlien(alienGroup, alienGroup.tapsToKill)
		end
	end

	if listeners[EVENT_ALIEN_KILLED] then
		listeners[EVENT_ALIEN_KILLED](total)
	end
end

aliens.gameStarted = function()
	log('gameStarted')

	gameEnded = false
	
	for i=1,group.numChildren do
		if group[i].alien then
			scaleDownAndRemoveAlien(group[i])
		end
	end
end

aliens.endCollision = function(alienGroup)
	log('endCollision')
	alienDidCollide(alienGroup)
end

aliens.gameEnded = function()
	log('gameEnded')

	gameEnded = true
end

aliens.create = function(occurrence, level)
	log('create')
	
	local alien = {}

	if occurrence > 0 and (occurrence % 20 == 0) then
		alien.type = 3
		alien.taps = 2
	else 
		alien.type = math.random(0,2)
		alien.taps = 1
	end

	alien.id       		= "" .. math.random()
	alien.rotation 		= math.random(-1,1) * 3
	alien.scale    		= 1.2
	alien.start    		= (math.random() * 0.8) + 0.15
	alien.speed    		= level.speed()
	alien.isKilled 		= false
	alien.didCollide 	= false

	return alien
end

aliens.dispose = function()
end

return aliens
