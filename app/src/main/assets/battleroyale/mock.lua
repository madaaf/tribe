local json = require 'json'

local myPeer    = { id='me',    display_name = 'Rémy',  picture='https://static.tribedev.pm/u/HJeY9YWMuM1519683984962.jpeg' }
local nicoPeer  = { id='nico',  display_name = 'Nico',  picture='https://static.tribedev.pm/u/r1tp__KiW1506539713340.jpeg' }
local tiagoPeer = { id='tiago', display_name = 'Tiago', picture='https://static.tribedev.pm/u/S1VWxBLTW1508425724462.jpeg' }
local madaPeer  = { id='mada',  display_name = 'Mada',  picture='https://static.tribedev.pm/u/H1t0WOvFb1504309712857.jpeg' }

local myEmptyPlayer = { id='me', color=null,   pos={ null, null }, score=null }
local myPlayer 		= { id='me', color='pink', pos={ 100, 100 },   score=10 }

local nicoEmptyPlayer = { id='nico', color=null,    pos={ null, null }, score=null }
local nicoPlayer 	  = { id='nico', color='green', pos={ 180, 180 },   score=8 }

local tiagoEmptyPlayer = { id='tiago', color=null,     pos={ null, null }, score=null }
local tiagoPlayer 	   = { id='tiago', color='yellow', pos={ 250, 250 },   score=11 }

local madaEmptyPlayer = { id='mada', color=null,     pos={ null, null }, score=null }
local madaPlayer 	  = { id='mada', color='red', pos={ 350, 400 },   score=10 }

local bulletTimer

local game = { board = { width=1000, height=1000 }, players = { } }
		
local exports = {}

local startGame

startGame = function (receive)

	game.players = {}

	game.players[1] = myEmptyPlayer
	receive('**NEW_PLAYER**' .. json.encode(myEmptyPlayer))

	timer.performWithDelay(500, function ()
		
		receive('**PEER_JOINED**' .. json.encode(nicoPeer))

		game.players[2] = nicoEmptyPlayer
		receive('**NEW_PLAYER**' .. json.encode(nicoEmptyPlayer))

		receive('**PEER_JOINED**' .. json.encode(tiagoPeer))

		game.players[3] = nicoEmptyPlayer
		receive('**NEW_PLAYER**' .. json.encode(tiagoEmptyPlayer))

		game.players[1] = myPlayer
		game.players[2] = nicoPlayer
		game.players[3] = tiagoPlayer
		timer.performWithDelay(200, function () receive('**GAME_READY**' .. json.encode(game)) end)
		
		timer.performWithDelay(3000, function () 
			receive('**GAME_START**' .. json.encode(game))

		--[[
			bulletTimer = timer.performWithDelay(1000, function () receive('&&b:nico,' .. math.random(1992092109) .. ',' .. nicoPlayer.pos[1] .. ',' .. nicoPlayer.pos[2] .. ',0,200,-1,m16') end, -1)

			timer.performWithDelay(15000, function ()
				timer.cancel(bulletTimer)
				bulletTimer = nil
				receive('&&s:nico,-1,0')
				receive('**PLAYER_LEFT**"nico"')
				game.players[2] = nil
				receive('**GAME_OVER**' .. json.encode(game))

				timer.performWithDelay(500, function ()
					startGame(receive)
				end)
			end)

			timer.performWithDelay(1000, function ()
				receive('&&u:r1LxkKbsG,potion_small,200,200')
				receive('&&w:r1LxkKbsG2,m16,300,300')
			end)
		]]
		end)
	end)
end

local function scenario1(data, receive)

	if data == '?' then
		receive('??' .. json.encode({ id = 'fake', state = 'IDLE', peers = { me = myPeer } }))
		receive('**GAME_LOADED**' .. json.encode(game))
		receive('**GAME_WAIT**' .. json.encode(game))

	elseif data == '+' and #game.players == 0 then
		startGame(receive)

	elseif data:starts('&') then

	else
		log(data)
	end
end

local function scenario2(data, receive)

	if data == '?' then
		game = { board = { width=1000, height=1000 }, players = { nico = nicoPlayer, tiago = tiagoPlayer, mada = madaPlayer } }
		receive('??' .. json.encode({ id = 'fake', state = 'PLAYING', game = game, peers = { me = myPeer, nico = nicoPeer, tiago = tiagoPeer, mada = madaPeer } }))
		
		Runtime:addEventListener("enterFrame", function ()
			tiagoPlayer.pos[2] = math.min(game.board.height, tiagoPlayer.pos[2] + 0.25)
			receive('&&p:tiago,' .. tiagoPlayer.pos[1] .. ',' .. tiagoPlayer.pos[2])
		end)

		timer.performWithDelay(3000, function () 
			receive('&&s:tiago,-1,0')
			receive('**PLAYER_LEFT**"tiago"')
		end)

		timer.performWithDelay(5000, function () 
			receive('&&s:mada,-1,0')
			receive('**PLAYER_LEFT**"mada"')
		end)

		timer.performWithDelay(7000, function () 
			startGame(receive)
		end)

	elseif data:starts('&') then

	else
		log(data)
	end
end

exports.dispatch = function (data, receive)
	scenario2(data, receive)
end

return exports
