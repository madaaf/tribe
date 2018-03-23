local loadedSounds  = {}
local loadedStreams = {}

local playingSoundtrack
local currentSoundtrackIndex

local model = require 'model'

---------------------------------------------------------------------------------

local exports = {}

exports.isVolumeEnabled = true
exports.shouldHandleSoundNatively = false

local function play(sound, params)
	if shouldHandleSoundNatively then
		return Runtime:dispatchEvent({ name='coronaView', event='playSound', sound=sound, params=params })
	else
		return audio.play(sound, params)
	end
end

local function stop(sound)
	if shouldHandleSoundNatively then
		Runtime:dispatchEvent({ name='coronaView', event='stopSound' })
	else
		audio.stop(sound)
	end
end

local function playSoundIfEnabled(sound, params) 
	if exports.isVolumeEnabled then
		return play(sound, params)
	else 
		return nil
	end
end

local function soundPath(path)
	if system.getInfo("platform") == "android" and system.pathForFile("assets/sounds/" .. path .. ".ogg") then
		return "assets/sounds/" .. path .. ".ogg"
	end

	return "assets/sounds/" .. path .. ".mp3"
end

exports.load = function()
	if system.getInfo("platform") == "android" then
		shouldHandleSoundNatively = false
		loadedSounds = {
			alienKilled = audio.loadSound(soundPath("alien_killed")),
			playerLost  = audio.loadSound(soundPath("player_lost")),
			playerWon   = audio.loadSound(soundPath("player_won")),
			watch       = audio.loadSound(soundPath("watch")),
		}
		loadedStreams = {
			audio.loadStream(soundPath("soundtrack_0")),
			audio.loadStream(soundPath("soundtrack_1")),
			audio.loadStream(soundPath("soundtrack_2")),
		}
	else
		shouldHandleSoundNatively = true
		loadedSounds = {
			alienKilled = soundPath("alien_killed"),
			playerLost  = soundPath("player_lost"),
			playerWon   = soundPath("player_won"),
			watch       = soundPath("watch"),
		}
		loadedStreams = {
			soundPath("soundtrack_0"),
			soundPath("soundtrack_1"),
			soundPath("soundtrack_2"),
		}
	end
end

exports.dispose = function()
	if shouldHandleSoundNatively then
		return
	end
	for i,s in ipairs(loadedSounds) do
		audio.stop(s)
		audio.dispose(s)
	end
	for i,s in ipairs(loadedStreams) do
		audio.stop(s)
		audio.dispose(s)
	end
end

exports.playSoundtrack = function(score)
	if exports.isVolumeEnabled then

		local level = model.levelByScore(score)
		if not (currentSoundtrackIndex == level.soundtrack) then
			
			if playingSoundtrack then
				stop(playingSoundtrack)
			end

			if not isSimulator then
				currentSoundtrackIndex = level.soundtrack
				playingSoundtrack = playSoundIfEnabled(loadedStreams[level.soundtrack], { loops=-1 })
			end

		end
	end
end

exports.stopSoundtrack = function()
	if playingSoundtrack then
		stop(playingSoundtrack)
		playingSoundtrack = nil
	end
	currentSoundtrackIndex = nil
end

exports.playWatch = function()
	playSoundIfEnabled(loadedSounds.watch)
end

exports.playAlienKilled = function()
	playSoundIfEnabled(loadedSounds.alienKilled)
end

exports.playPlayerWon = function()
	playSoundIfEnabled(loadedSounds.playerWon)
end

exports.playPlayerLost = function()
	playSoundIfEnabled(loadedSounds.playerLost)
end

return exports
