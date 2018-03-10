-- Lua

local composer = require "composer"
local physics  = require "physics"

-- Local

local sounds     = require "sounds"
local texts      = require "texts"
local messenger  = require "messenger"
local vibrator   = require "vibrator"

local background = require "background"
local aliens     = require "aliens"
local model      = require "model"
local bonus      = require "bonus"

---------------------------------------------------------------------------------

local scene = composer.newScene()

local myUserId, masterUserId
local playersIds    = {}
local playersById   = {}
local playingIds    = {}
local playersScores = {}

local previousOccurrence = 0

local createAlienTimer

local paceFactorTimer
local aliensPaceFactor = 1

---------------------------------------------------------------------------------

local function isMaster() 
	return myUserId == masterUserId
end

local function broadcastUpdatedScores() 
	log('broadcastUpdatedScores')

	for i,id in ipairs(playersIds) do
		if not playersScores[id] then
			playersScores[id] = 0
		end
	end

	if isMaster() then
		messenger.broadcastScores(playersScores)
		
	else
		-- Reduced payload
		local message = {}
		message[myUserId] = playersScores[myUserId] 
		messenger.broadcastScores(message)
	end
end

local function scoresUpdated(updatedPlayersScores)
	log('scoresUpdated')

	for userId,score in pairs(updatedPlayersScores) do
		playersScores[userId] = score
	end

	Runtime:dispatchEvent({ name='coronaView', event='scoresUpdated', scores=playersScores })
end

local function resetScores()
	log('resetScores')

	playersScores = {}
	broadcastUpdatedScores()
end

---------------------------------------------------------------------------------

local function getStartGameTimestamp()
	return os.time() + 5
end

local function getDelayUntilTimestamp(timestamp)
	return math.max(0, timestamp - os.time()) * 1000
end

---------------------------------------------------------------------------------

local function toggleVolume(event)
	log('toggleVolume')

	if not (sounds.isVolumeEnabled == event.isEnabled) then
		
		sounds.isVolumeEnabled = event.isEnabled

		if not event.isEnabled then
			sounds.stopSoundtrack()
		else
			sounds.playSoundtrack(0)
		end
	end
end

local function startGame(event) 
	log('startGame')

	myUserId     = event.myUserId
	masterUserId = event.masterUserId

	playersById = {}
	for i,u in ipairs(event.playersUsers) do
		playersById[u.id] = u
		table.insert(playersIds, u.id)
	end

	sounds.isVolumeEnabled = event.isVolumeEnabled
	sounds.playSoundtrack(0)

	timer.performWithDelay(500, function() 
		messenger.broadcastUserReady()
		
		if isMaster() then
			resetScores()
			messenger.broadcastNewGame(myUserId, getStartGameTimestamp(), playersIds)
		end
	end)
end

local function userJoined(event) 
	log('userJoined')
	
	local user = event.user

	playersById[user.id] = user
	table.insert(playersIds, user.id)

	if isMaster() then
		broadcastUpdatedScores()
	end
end

local function userLeft(event) 
	log('userLeft')
	
	local userId = event.userId
	
	playingIds[userId]    = nil
	playersById[userId]   = nil
	playersScores[userId] = nil

	if masterUserId == userId then
		reelectNewMaster()
	end
end

---------------------------------------------------------------------------------

local function addPointsToScore(points)
	log('addPointsToScore')

	local score = playersScores[myUserId]
	if score then
		score = score + points
	else
		score = points
	end

	if score > 0 then
		if score % 100 == 0 then
			bonus.showBomb()
		elseif score % 50 == 0 then
			bonus.showWatch()
		end
	end

	playersScores[myUserId] = score
	background.switchGradient(score)
	sounds.playSoundtrack(score)

	broadcastUpdatedScores()
end

---------------------------------------------------------------------------------

local function reelectNewMaster()
	log('reelectNewMaster')

	table.sort(playersIds)

	masterUser = playersById[playersIds[0]]
	
	if isMaster() then
		takeOverGame()
	end
end

local function takeOverGame()
	log('takeOverGame')

	broadcastUpdatedScores()

	if next(playingIds) == nil then
		messenger.broadcastGameOver(nil)
	else
		createAlien(previousOccurrence)
	end
end

---------------------------------------------------------------------------------

local function useBomb()
	log('useBomb')

	aliens.killAliens()
	background.shake()
end

local function useWatch()
	log('useWatch')
	
	sounds.playWatch()

	aliensPaceFactor = 3
	aliens.changeAliensSpeed(3)

	paceFactorTimer = timer.performWithDelay(5000, function ()
		aliensPaceFactor = 1
		aliens.changeAliensSpeed(1)
	end)
end

---------------------------------------------------------------------------------

local function createAlien(occurrence)
	log('createAlien')

	local level = model.levelByScore(occurrence)
	local alien = aliens.create(occurrence, level)

	messenger.broadcastPopAlien(alien, occurrence)

	createAlienTimer = timer.performWithDelay(level.popInterval() * 1000, function () createAlien(occurrence + 1) end)
end

local function newGame(fromUserId, timestamp, playersIds)
	log('newGame')

	aliens.gameStarted()
	background.resetGradient()
	sounds.playSoundtrack(0)

	masterUserId = fromUserId

	playingIds = {}
	for i,v in ipairs(playersIds) do
		playingIds[v] = true
	end

	texts.showTitle({ onComplete=function ()
		texts.showLetsGo()
	end })

	previousOccurrence = 0
	if isMaster() then
		timer.performWithDelay(getDelayUntilTimestamp(timestamp), function ()
			createAlien(0)
		end)
	end
end

local function popAlien(alien, occurrence) 

	if occurence then
		log('popAlien - ' .. occurrence)
		previousOccurrence = occurrence
	else
		log('popAlien')
		previousOccurrence = previousOccurrence + 1
	end
	
	if (previousOccurrence % aliensPaceFactor) == 0 then
		aliens.pop(alien, aliensPaceFactor)
	end
end

local function userGameOver(userId)
	log('userGameOver')

	if playingIds[userId] then
		playingIds[userId] = nil

		if isMaster() then
			-- No-one is playing anymore
			if next(playingIds) == nil then
				messenger.broadcastGameOver(userId)
			else
				messenger.broadcastShowUserLost(userId)
			end
		end
	end
end

local function showUserLost(userId)
	log('showUserLost')

	sounds.playPlayerLost()

	if userId == myUserId then
		texts.showYouLost()
	else
		local user = playersById[userId]
		texts.showSomeoneLost(user.displayName)
	end
end

local function becomeGameMaster()
	log('becomeGameMaster')

	resetScores()
	messenger.broadcastNewGame(myUserId, getStartGameTimestamp(), playersIds)
end

local function gameOver(winnerId)
	log('gameOver')

	aliensPaceFactor = 1

	if paceFactorTimer  then timer.cancel(paceFactorTimer)  end
	if createAlienTimer then timer.cancel(createAlienTimer) end
	
	sounds.playPlayerWon()

	if winnerId then
		if winnerId == myUserId then
			if #playersIds > 1 then
				texts.showYouWon({ onComplete=becomeGameMaster })
			else
				texts.showYouLost({ onComplete=becomeGameMaster })
			end

		else
			local winner = playersById[winnerId]
			texts.showSomeoneWon(winner.displayName)
		end

	else
		if fromUserId == myUserId or not fromUserId then
			texts.showGameOver({ onComplete=becomeGameMaster })
		else
			texts.showGameOver()
		end
	end
end

---------------------------------------------------------------------------------

local function alienKilled(points) 
	log('alienKilled')

	sounds.playAlienKilled()
	addPointsToScore(points)
	vibrator.sendImpact()
end

local function alienReachedTheGround() 
	log('alienReachedTheGround')

	local score = playersScores[myUserId]
	if score then
		Runtime:dispatchEvent({ name='coronaView', event='saveScore', score=score })
	end

	bonus.removeBonuses()
	aliens.gameEnded()
	messenger.broadcastUserGameOver(myUserId)
end

---------------------------------------------------------------------------------

function scene:create(event)
	log('scene:create')

	physics.start()
	physics.pause()

	sounds.load()

	local sceneGroup = self.view
	sceneGroup:insert(background.load())
	sceneGroup:insert(aliens.load())

	messenger.addMessageListener('newGame',       newGame)
	messenger.addMessageListener('popAlien',      popAlien)
	messenger.addMessageListener('userGameOver',  userGameOver)
	messenger.addMessageListener('showUserLost',  showUserLost)
	messenger.addMessageListener('gameOver',	  gameOver)
	messenger.addMessageListener('scoresUpdated', scoresUpdated)

	aliens.addAlienListener('alienKilled', 			 alienKilled)
	aliens.addAlienListener('alienReachedTheGround', alienReachedTheGround)

	bonus.addBonusListener('useBomb',  useBomb)
	bonus.addBonusListener('useWatch', useWatch)

	Runtime:addEventListener('startGame',    startGame)
	Runtime:addEventListener('userJoined',   userJoined)
	Runtime:addEventListener('userLeft',     userLeft)
	Runtime:addEventListener('toggleVolume', toggleVolume)
end

function scene:show(event)
	log('scene:show ' .. event.phase)

	if event.phase == "did" then
		
		physics.start()

		Runtime:dispatchEvent({ name='coronaView', event='gameLoaded' })

		if isSimulator then
			local user = { id='toto', displayName='TOTO', username='toto' }
			-- local user2 = { id='toto2', displayName='TOTO2', username='toto2' }
			startGame({ myUserId=user.id, masterUserId=user.id, playersUsers={user}, isVolumeEnabled=true })
		end
	end
end

function scene:hide(event)
	log('scene:hide ' .. event.phase)

	if event.phase == "will" then

		physics.stop()
	end
end

function scene:destroy(event)
	log('scene:destroy')

	sounds.dispose()
	background.dispose()
	aliens.dispose()
	
	package.loaded[physics] = nil
	physics = nil
end

---------------------------------------------------------------------------------

scene:addEventListener('create',  scene)
scene:addEventListener('show',    scene)
scene:addEventListener('hide',    scene)
scene:addEventListener('destroy', scene)

--physics.setDrawMode("hybrid")

---------------------------------------------------------------------------------

return scene
