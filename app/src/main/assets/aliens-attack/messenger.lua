local ACTION_POP_ALIEN = "popAlien"
local ALIEN_KEY        = "alien"
local FROM_KEY         = "from"
local PLAYERS_KEY      = "players"
local TIMESTAMP_KEY    = "timestamp"

local CONTEXT_KEY    = "context"
local SCORES_KEY     = "scores"

local ACTION_KEY     = "action"
local USER_KEY       = "user"
local OCCURRENCE_KEY = "occurrence"

local ACTION_NEW_GAME       = "newGame"
local ACTION_USER_GAME_OVER = "userGameOver"
local ACTION_USER_READY     = "userReady"
local ACTION_SHOW_USER_LOST = "showUserLost"
local ACTION_GAME_OVER      = "gameOver"

local listeners = {}

---------------------------------------------------------------------------------

local function receiveMessage(event)
		
	local fromUserId = event.fromUserId
	local message = event.message

	if message then

		local action  = message[ACTION_KEY]
		local context = message[CONTEXT_KEY]

		if action then

			local listener = listeners[action]
			if listener then

				if action == ACTION_NEW_GAME then
					listener(message[FROM_KEY], message[TIMESTAMP_KEY], message[PLAYERS_KEY])

				elseif action == ACTION_POP_ALIEN then
					listener(message[ALIEN_KEY], message[OCCURRENCE_KEY])

				elseif action == ACTION_USER_GAME_OVER then
					listener(message[USER_KEY])

				elseif action == ACTION_USER_READY then
					listener(message[USER_KEY])
				
				elseif action == ACTION_SHOW_USER_LOST then
					listener(message[USER_KEY])

				elseif action == ACTION_GAME_OVER then
					listener(message[USER_KEY])
				end
			end

		elseif context then

			local scores = message[SCORES_KEY]
			if context == 'scores' and scores then

				local listener = listeners['scoresUpdated']
				if listener then
					listener(scores)
				end
			end
		end
	end
end

Runtime:addEventListener('receiveMessage', receiveMessage)

local function broadcastMessage(message)
	log('broadcastMessage')

	local event = { name='coronaView', event='broadcastMessage', message=message }
	Runtime:dispatchEvent(event)
	receiveMessage(event)
end

local function sendMessage(message, to)
	log('sendMessage')

	Runtime:dispatchEvent({ name='coronaView', event='sendMessage', message=message, to=to })
end

---------------------------------------------------------------------------------

local exports = {}

exports.addMessageListener = function(action, listener)
	listeners[action] = listener
end

exports.broadcastScores = function(playersScores)

	local message = {}
	message[CONTEXT_KEY] = 'scores'
	message[SCORES_KEY]  = playersScores

	broadcastMessage(message)

	local event = { name='coronaView', event='contextGame', context={ scores=playersScores } }
	Runtime:dispatchEvent(event)
end

exports.broadcastNewGame = function(fromUserId, timestamp, playersIds)

	local message = {}
	message[ACTION_KEY]    = ACTION_NEW_GAME
	message[FROM_KEY]      = fromUserId
	message[TIMESTAMP_KEY] = timestamp
	message[PLAYERS_KEY]   = playersIds

    broadcastMessage(message)
end

exports.broadcastPopAlien = function(alien, occurrence)

	local message = {}
	message[ACTION_KEY]     = ACTION_POP_ALIEN
	message[ALIEN_KEY]      = alien
	message[OCCURRENCE_KEY] = occurrence

    broadcastMessage(message)
end

exports.broadcastUserGameOver = function(userId)

	local message = {}
	message[ACTION_KEY] = ACTION_USER_GAME_OVER
	message[USER_KEY]   = userId

    broadcastMessage(message)
end

exports.broadcastUserReady = function(userId)

	local message = {}
	message[ACTION_KEY] = ACTION_USER_READY
	message[USER_KEY]   = userId

    broadcastMessage(message)
end

exports.broadcastShowUserLost = function(userId)

	local message = {}
	message[ACTION_KEY] = ACTION_SHOW_USER_LOST
	message[USER_KEY]   = userId

    broadcastMessage(message)
end

exports.broadcastGameOver = function(winnerId)

	local message = {}
	message[ACTION_KEY] = ACTION_GAME_OVER
	message[USER_KEY]   = winnerId

    broadcastMessage(message)
end

return exports
