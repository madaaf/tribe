local loadedSounds = {}
local loadedStreams = {}

local playingSoundtrack
local currentSoundtrackIndex

local model = require 'model'

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
        alienKilled = audio.loadSound("assets/sounds/alien_killed.ogg"),
        playerLost = audio.loadSound("assets/sounds/player_lost.ogg"),
        playerWon = audio.loadSound("assets/sounds/player_won.ogg"),
        watch = audio.loadSound("assets/sounds/watch.ogg"),
    }
    loadedStreams = {
        audio.loadStream("assets/sounds/soundtrack_0.ogg"),
        audio.loadStream("assets/sounds/soundtrack_1.ogg"),
        audio.loadStream("assets/sounds/soundtrack_2.ogg"),
    }
end

exports.dispose = function()
    for i, s in ipairs(loadedSounds) do
        audio.stop(s)
        audio.dispose(s)
    end
    for i, s in ipairs(loadedStreams) do
        audio.stop(s)
        audio.dispose(s)
    end
end

exports.playSoundtrack = function(score)
    log('playSoundtrack')

    if exports.isVolumeEnabled then

        local level = model.levelByScore(score)
        if not (currentSoundtrackIndex == level.soundtrack) then

            if playingSoundtrack then
                audio.stop(playingSoundtrack)
            end

            currentSoundtrackIndex = level.soundtrack
            playingSoundtrack = playSoundIfEnabled(loadedStreams[level.soundtrack], { loops = -1 })
        end
    end
end

exports.stopSoundtrack = function()
    log('stopSoundtrack')

    if playingSoundtrack then
        audio.stop(playingSoundtrack)
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
