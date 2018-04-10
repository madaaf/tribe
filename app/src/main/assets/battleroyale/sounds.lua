---------------------------------------------------------------------------------
-- Parameters

local playingSoundtrack
local loadedSounds  = {}
local loadedStreams = {}
local exports = {}
exports.isVolumeEnabled = true
exports.shouldHandleSoundNatively = false

---------------------------------------------------------------------------------
-- Local Functions

local function play(sound, params)
	if shouldHandleSoundNatively then
		return Runtime:dispatchEvent({ name='coronaView', event='playSound', sound=sound, params=params })
	else
		return audio.play(sound, params)
	end
end

local function stop(sound)
	log('sounds - stop')
	if shouldHandleSoundNatively then
		Runtime:dispatchEvent({ name='coronaView', event='stopSound' })
	else 
		if sound then
			audio.stop(sound)
		end
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

---------------------------------------------------------------------------------
-- Export Functions

exports.load = function()
	if system.getInfo("platform") == "android" then
		shouldHandleSoundNatively = false
		loadedSounds = {
			die 		 = audio.loadSound(soundPath("die")),
			bonusUtility = audio.loadSound(soundPath("bonus_utility")),
			bonusWeapon  = audio.loadSound(soundPath("bonus_weapon")),
			shotByBullet = audio.loadSound(soundPath("shot_by_bullet")),
			shotDefault  = audio.loadSound(soundPath("shot_default")),
			shotRocket   = audio.loadSound(soundPath("shot_rocket")),
			shotShotgun  = audio.loadSound(soundPath("shot_shotgun"))
		}
		loadedStreams = {
			soundtrack = audio.loadStream(soundPath("soundtrack"))
		}
	else
		shouldHandleSoundNatively = true
		loadedSounds = {
			die 		 = soundPath("die"),
			bonusUtility = soundPath("bonus_utility"),
			bonusWeapon  = soundPath("bonus_weapon"),
			shotByBullet = soundPath("shot_by_bullet"),
			shotDefault  = soundPath("shot_default"),
			shotRocket   = soundPath("shot_rocket"),
			shotShotgun  = soundPath("shot_shotgun")
		}
		loadedStreams = {
			soundtrack = soundPath("soundtrack")
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

exports.playSoundtrack = function()
	if exports.isVolumeEnabled and not playingSoundtrack and not isSimulator then
		playingSoundtrack = playSoundIfEnabled(loadedStreams.soundtrack, { loops=-1 })
	end
end

exports.stopSoundtrack = function()
	if playingSoundtrack then
		stop(playingSoundtrack)
		playingSoundtrack = nil
	end
end

exports.playDie = function()
	playSoundIfEnabled(loadedSounds.die)
end

exports.playBonusWeapon = function()
	playSoundIfEnabled(loadedSounds.bonusWeapon)
end

exports.playBonusUtility = function()
	playSoundIfEnabled(loadedSounds.bonusUtility)
end

exports.playShotByBullet = function()
	playSoundIfEnabled(loadedSounds.shotByBullet)
end

exports.playShot = function(weapon)
	if weapon.subtype == 'shotgun' then
		playSoundIfEnabled(loadedSounds.shotShotgun)
	elseif weapon.subtype == 'rocket' then
		playSoundIfEnabled(loadedSounds.shotRocket)
	else
		playSoundIfEnabled(loadedSounds.shotDefault)
	end
end

return exports
