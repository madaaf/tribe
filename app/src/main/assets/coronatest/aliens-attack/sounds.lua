local loadedSounds = {}

local playingSoundtrack

---------------------------------------------------------------------------------

local exports = {}

exports.isVolumeEnabled = true

local function playSoundIfEnabled(sound, params) 
	if exports.isVolumeEnabled then
		return audio.play(sound, params)
	else 
		return nil
	end
end

exports.load = function()
	loadedSounds = {
		soundtrack  = audio.loadStream("assets/sounds/soundtrack.mp3"),
		alienKilled = audio.loadSound("assets/sounds/alien_killed.mp3"),
		playerLost  = audio.loadSound("assets/sounds/player_lost.mp3"),
		playerWon   = audio.loadSound("assets/sounds/player_won.mp3"),
		watch       = audio.loadSound("assets/sounds/watch.mp3"),
	}
end

exports.dispose = function()
	for i,s in ipairs(loadedSounds) do
		audio.stop(s)
		audio.dispose(s)
	end
end


exports.playSoundtrack = function()
	log('playSoundtrack')
	
	playingSoundtrack = playSoundIfEnabled(loadedSounds.soundtrack, { loops=-1 })
end

exports.stopSoundtrack = function()
	log('stopSoundtrack')

	if playingSoundtrack then
		audio.stop(playingSoundtrack)
		playingSoundtrack = nil
	end
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
