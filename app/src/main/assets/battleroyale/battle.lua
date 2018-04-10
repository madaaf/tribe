local composer 		= require 'composer'
local physics 		= require 'physics'

local gamemaster 	  = require 'gamemaster'
local users 	  	  = require 'users'
local texts 		  = require 'texts'
local perspective 	  = require 'perspective'
local utils 		  = require 'utils.utils'
local bodiesLibrary   = require 'engine2D.bodies'
local bonusLibrary    = require 'bonus.bonus'
local mapLibrary 	  = require 'map.map'
local miniMapLibrary  = require 'minimap.minimap'
local joystickLibrary = require 'joysticks.joysticks'
local colors 	 	  = require 'colors'
local sounds 	 	  = require 'sounds'

---------------------------------------------------------------------------------
-- Parameters

local scene = composer.newScene()

local screenW, screenH = display.contentWidth, display.contentHeight
local screenHalfW, screenHalfH = screenW/2, screenH/2

local camera = perspective.createView()
local gameGroup = display.newGroup()
local playersGroup = display.newGroup()
local viewerMode = display.newImageRect('assets/images/viewer_mode.png', screenW, screenH)
viewerMode.alpha = 0
viewerMode.anchorX, viewerMode.anchorY = 0,0
local map, miniMap

---------------------------------------------------------------------------------
-- DEBUG

local sessionStateText = display.newText('', 0, 0, 'CorporativeSansRdAltBlack', 10)
sessionStateText:setFillColor( 1, 1, 1, 0.25 )
sessionStateText.anchorY = 0
sessionStateText.x = screenW/2
sessionStateText.y = safeScreenOriginY + 76

---------------------------------------------------------------------------------
-- Sound

local function toggleVolume(event)
	if not (sounds.isVolumeEnabled == event.isEnabled) then
		sounds.isVolumeEnabled = event.isEnabled
		if not event.isEnabled then
			sounds.stopSoundtrack()
		else
			sounds.playSoundtrack()
		end
	end
end

---------------------------------------------------------------------------------
-- World

local function createMap(worldSize)

	map = mapLibrary.newMap({ width = worldSize.width, height = worldSize.height, radius = 10 })
	gameGroup:insert(map)
end

local function createMiniMap(worldSize)
	miniMap = miniMapLibrary.newMiniMap({ worldWidth = worldSize.width, worldHeight = worldSize.height, fixedHeight = 60 })
end

local function createWalls(worldSize)

	local wallSize = math.max(screenW, screenH)

	local leftWall = display.newRect(0, 0, wallSize, worldSize.height)
	leftWall.anchorX, leftWall.anchorY = 1, 0
	leftWall:setFillColor(0, 0, 0, 1)

	local topWall = display.newRect(-worldSize.width/2, 0, worldSize.width * 2, wallSize)
	topWall.anchorX, topWall.anchorY = 0, 1
	topWall:setFillColor(0, 0, 0, 1)

	local rightWall = display.newRect(worldSize.width, 0, wallSize, worldSize.height)
	rightWall.anchorX, rightWall.anchorY = 0, 0
	rightWall:setFillColor(0, 0, 0, 1)

	local bottomWall = display.newRect(-worldSize.width/2, worldSize.height, worldSize.width * 2, wallSize)
	bottomWall.anchorX, bottomWall.anchorY = 0, 0
	bottomWall:setFillColor(0, 0, 0, 1)

	return { leftWall, topWall, rightWall, bottomWall }
end

---------------------------------------------------------------------------------
-- Frames handler

local function normalize(value, min, max) 
	return math.abs((value - 0) / (max - min));
end

local DEFAULT_SPEED = 15.0
local speed = DEFAULT_SPEED

local function observePosition(worldSize)

	local previousPosition = { x=0, y=0 }

	Runtime:addEventListener('enterFrame', function ()

		local data = {}
		
		local player = users.myPlayer
		if player then

			-- Move the players 
			player:setDeltaMove(joystickLibrary.move(player.image, speed))
			if player.weapon.image then
				joystickLibrary.rotate(player.weapon.image)
			end

			player:move(player.image.x, player.image.y)

			-- Move the camera
			camera:trackFocus()

			if previousPosition.x ~= player.image.x or previousPosition.y ~= player.image.y then
				previousPosition = { x=player.image.x, y=player.image.y }
				gamemaster.broadcastPosition(player.image.x, player.image.y)
			else
				--TODO : Broadcast a lighter message (a ping ?)
				gamemaster.broadcastPosition(player.image.x, player.image.y)
			end
		end
	end)
end

---------------------------------------------------------------------------------
-- Viewer mode

local function showViewerMode()
	transition.fadeIn(viewerMode)
end

local function hideViewerMode()
	transition.fadeOut(viewerMode)
end

---------------------------------------------------------------------------------
-- Splash

local splashMap
local function showSplash()
	splashMap = mapLibrary.newMap({ width = 1000, height = 1000, radius = 10 })
	camera:add(splashMap, 4)

	texts.showTitle(function ()
	 	gamemaster.start()
	end)
end

---------------------------------------------------------------------------------
-- Weapons

local function shotBullet(bulletId, x, y, angle, weapon)
	gamemaster.addBullet(bulletId, x, y, angle, weapon)
	bodiesLibrary.addBullet(gameGroup, gamemaster, users.myPlayer, users.myPlayer, bulletId, x, y, angle, weapon)
	sounds.playShot(weapon)
end

local shootTimer = nil
local function chargeWeapon(weapon, animated)

	if shootTimer then
		timer.cancel(shootTimer)
	end

	if users.myPlayer then
		users.myPlayer:setWeapon(weapon, animated)
	end
	
	shootTimer = timer.performWithDelay(weapon.shootingDelay, function ()

		-- if shootAngle then
		local player = users.myPlayer
		if joystickLibrary.getMoving() and player then

			local bulletId = utils.generateId()
			local angle = joystickLibrary.getAngleRad()
			local x = player.image.x + ((40 + weapon.bulletOffset) * math.sin(angle))
			local y = player.image.y + ((40 + weapon.bulletOffset) * -math.cos(angle))

			shotBullet(bulletId, x, y, angle, weapon)

			if (weapon.subtype == 'shotgun') then
				bulletId = utils.generateId()
				shotBullet(bulletId, x, y, angle + 0.15, weapon)
				bulletId = utils.generateId()
				shotBullet(bulletId, x, y, angle - 0.15, weapon)
			elseif (weapon.subtype == 'm16') then
				bulletId = utils.generateId()
				shotBullet(bulletId, x, y, angle + 3.14, weapon)
			end
		end

	end, -1)
end

---------------------------------------------------------------------------------
-- Utilities

local function chargeUtility(id, utility)

	if utility.property_change == 'points' then
		if users.myPlayer then
			gamemaster.getBonusUtility(id, utility.value, users.myPlayer:scoreByAddingPoints(utility.value, true))
		end
	elseif utility.property_change == 'speed' then
		speed = speed * 2
	end
end

---------------------------------------------------------------------------------
-- Game

local function createWorld(game)

	local worldSize = { width=game.board.width, height=game.board.height }

	local walls = createWalls(worldSize)
	
	joystickLibrary.create()
	createMiniMap(worldSize)
	createMap(worldSize)

	observePosition(worldSize)

	bodiesLibrary.addWall(walls[1])
	bodiesLibrary.addWall(walls[2])
	bodiesLibrary.addWall(walls[3])
	bodiesLibrary.addWall(walls[4])
end

---------------------------------------------------------------------------------
-- Session Flow

local gameCreated = false

local function isPlayerInGame(game)

	if game.players then
		for k,v in pairs(game.players) do
			if v.id == users.myUserId then
				return true
			end
		end
	end

	return false
end

local waitingTimer

local function updateSessionState(state, game)

	sessionStateText.text = state

	if game then
		if not gameCreated then
			gameCreated = true
			createWorld(game)
		end
	end

	if waitingTimer then
		timer.cancel(waitingTimer)
		waitingTimer = nil
	end

	if state == 'WAITING' then 
		users.resetStatuses()
		gamemaster.joinPlayers()

		if users.isAlone() then
			physics.start()
		
			joystickLibrary.show()
			joystickLibrary:showTutorial()
			showViewerMode()

			users.createTrainingPlayer(playersGroup, miniMap, camera)
			chargeWeapon(bonusLibrary.defaultWeapon(), true)
			
			waitingTimer = timer.performWithDelay(1500, function ()
				texts.showWaitingInstructions()
				joystickLibrary:hideTutorial()
			end)
		end

	elseif state == 'IDLE' then
		-- Normally, the game is already loaded, but in case of network problems, the status will be reset to IDLE.
		-- In that case, we relaunch the game.
		gamemaster.loadGame()

	elseif state == 'FINISHING' then
		joystickLibrary:hide()
		users.removeAllPlayers()

	elseif state == 'STARTING' then 
		physics.pause()

		users.resetStatuses()
		users.removeAllPlayers()
		
		joystickLibrary.hide()
		joystickLibrary:hideTutorial()
		hideViewerMode()

		if isPlayerInGame(game) then
			texts.showStartingInstructions()
			users.showStartingPlayers(playersGroup, miniMap, camera, game)
		end

	elseif state == 'PLAYING' then
		physics.start()

		if isPlayerInGame(game) then
			joystickLibrary.show()
			chargeWeapon(bonusLibrary.defaultWeapon(), false)
		else
			showViewerMode()
			users.showPlayingPlayers(playersGroup, miniMap, camera, game)
			texts.showPendingInstructions()
		end
	end
end

local function onSessionEvent(event) 
	users.updatePeers(event.session)
	updateSessionState(event.session.state, event.session.game)

	if splashMap then
		camera:remove(splashMap)
		display.remove(splashMap)
		splashMap = nil
	end
end

local function onGameEvent(event) 
	
	-- Game statuses

	if     event.name == 'GAME_LOADED' then updateSessionState('WAITING',   event.payload)
	elseif event.name == 'GAME_READY'  then updateSessionState('STARTING',  event.payload)
	elseif event.name == 'GAME_START'  then updateSessionState('PLAYING',   event.payload)
	elseif event.name == 'GAME_OVER'   then updateSessionState('FINISHING', event.payload)
	elseif event.name == 'GAME_WAIT'   then updateSessionState('WAITING',   event.payload)
		
	-- Players

	-- elseif event.name == 'NEW_PLAYER'  then users.createOrUpdatePlayer(playersGroup, miniMap, camera, event.payload)
	elseif event.name == 'PLAYER_LEFT' then users.removePlayer(event.payload, camera)

	-- Peers

	elseif event.name == 'PEER_JOINED' then users.addPeer(event.payload)
	elseif event.name == 'PEER_LEFT'   then users.removePeer(event.payload.id)
	end
end

local function onPositionEvent(event) 

	if event.userId ~= users.myUserId then
		local player = users.getPlayer(event.userId)
		if player then

			local x = (player.image.x + event.x) / 2
			local y = (player.image.y + event.y) / 2

			player:move(x, y)

			camera:trackFocus()
		end
	end
end

local function onAddBulletEvent(event) 

	if event.userId ~= users.myUserId then
		local player = users.getPlayer(event.userId)
		if player then
			local weapon = bonusLibrary.getBonus('weapon', event.weaponSubtype)
			bodiesLibrary.addBullet(gameGroup, gamemaster, users.myPlayer, player, event.id, event.x, event.y, event.angle, weapon)
		end
	end
end

local function onRemoveBulletEvent(event) 
	
	if event.userId ~= users.myUserId then
		local player = users.getPlayer(event.userId)
		if player then
			player:shot()
		end

		bodiesLibrary.removeBody(event.id)
	end
end

local function dontHaveBonusEnabled()
	return speed == DEFAULT_SPEED and users.myPlayer and users.myPlayer.weapon and users.myPlayer.weapon.weapon.subtype == 'default'
end

local function onBonusEvent(event) 
	local id = event.id
	local bonus = bodiesLibrary.addBonus(gameGroup, event.x, event.y, event.type, event.subtype, id)

	timer.performWithDelay(5000, function ()
		bodiesLibrary.removeBody(id)
	end)

	bonus:addEventListener('collision', function (event)
		bodiesLibrary.removeBody(id)

		if bonus.item.timer then

			users.myPlayer:displayBonusEnabled(true)

			timer.performWithDelay(bonus.item.timer, function ()
				if bonus.bonusType == 'weapon' then
					chargeWeapon(bonusLibrary.defaultWeapon())
				elseif bonus.item.property_change == 'speed' then
					speed = DEFAULT_SPEED
				end

				-- No more bonuses
				if dontHaveBonusEnabled() then
					users.myPlayer:displayBonusEnabled(false)
				end
			end)
		end

		-- TODO : Challenge before loading

		if bonus.bonusType == 'weapon' then
			gamemaster.getBonusWeapon(id)
			chargeWeapon(bonus.item)
			sounds.playBonusWeapon()
		else
			chargeUtility(id, bonus.item)
			sounds.playBonusUtility()
		end
	end)
end

local function onGetBonusEvent(event)

	local id = event.id
	local userId = event.userId

	if event.userId ~= users.myUserId then
		local player = users.getPlayer(event.userId)
		local bonus = bodiesLibrary.getBody(id)
		if bonus then

			if bonus.bonusType == 'weapon' then
				player:setWeapon(bonus.item)
			end

			if bonus.item.timer then
				player:displayBonusEnabled(true)
				timer.performWithDelay(bonus.item.timer, function ()
					if bonus.bonusType == 'weapon' then
						player:setWeapon(bonusLibrary.defaultWeapon())
					end
					player:displayBonusEnabled(false)
				end)
			end
		end

		bodiesLibrary.removeBody(id)
	end
end

local function onScoreEvent(event) 
	
	if event.userId ~= users.myUserId then
		local player = users.getPlayer(event.userId)
		if player then
			player:updateScore(event.score, false)
		end
	end
end

local function onConnectionIssueEvent()

	texts.showConnectionIssue()
end

---------------------------------------------------------------------------------
-- Game Flow

function scene:startGame(event)
	users.myUserId = event.myUserId
	sounds.isVolumeEnabled = event.isVolumeEnabled
	sounds.playSoundtrack()
	showSplash()
end

---------------------------------------------------------------------------------
-- Scene

function scene:create(event)
	
	if system.getInfo('environment') ~= 'simulator' then
		system.activate('multitouch')
	end

	--physics.setDrawMode( "hybrid" )
	physics.start()
	physics.pause()
	physics.setGravity(0, 0)
	sounds.load()
	
	local sceneGroup = self.view
	
	camera:add(playersGroup, 2)
	camera:add(gameGroup, 	 3)
	camera:track()

	gamemaster.setEventListener('onSessionEvent',      onSessionEvent)
	gamemaster.setEventListener('onGameEvent',         onGameEvent)
	gamemaster.setEventListener('onPositionEvent',     onPositionEvent)
	gamemaster.setEventListener('onAddBulletEvent',    onAddBulletEvent)
	gamemaster.setEventListener('onRemoveBulletEvent', onRemoveBulletEvent)
	gamemaster.setEventListener('onScoreEvent',		   onScoreEvent)
	gamemaster.setEventListener('onBonusEvent',		   onBonusEvent)
	gamemaster.setEventListener('onGetBonusEvent', 	   onGetBonusEvent)
	gamemaster.setEventListener('onConnectionIssueEvent', onConnectionIssueEvent)

	Runtime:addEventListener('startGame', 	 scene)
	Runtime:addEventListener('toggleVolume', toggleVolume)
end

function scene:show(event)
	if event.phase == "did" then
		physics.start()

		Runtime:dispatchEvent({ name='coronaView', event='gameLoaded' })
		if isSimulator then
			self:startGame({ myUserId = 'me' })
		end
	end
end

function scene:hide(event)
	if event.phase == "will" then
		physics.stop()
	end
end

function scene:destroy(event)
	package.loaded[physics] = nil
	physics = nil
end

---------------------------------------------------------------------------------

scene:addEventListener('create',  scene)
scene:addEventListener('show',    scene)
scene:addEventListener('hide',    scene)
scene:addEventListener('destroy', scene)

---------------------------------------------------------------------------------

return scene
