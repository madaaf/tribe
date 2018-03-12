local json = require("json")

local base = system.CachesDirectory
local scores_file = "scores_data.json"
local revive_file = "revive_data.json"

---------------------------------------------------------------------------------

local persistenceStore = {}

persistenceStore.allScores = function()

    log('persistenceStore - allScores')

    local path = system.pathForFile(scores_file, base)
    local contents = ""
    local savedScores = {}
    local file = io.open(path, "r")

    if file then
        -- read all contents of file into a string
        local contents = file:read( "*a" )
        savedScores = json.decode(contents)
        io.close( file )
    end

    return savedScores
end

persistenceStore.saveScore = function(score, gameId)
    
    log('persistenceStore - saveScore ' .. score .. ' for ' .. gameId)

    local savedScores = {}
    local tmpSavedScores = persistenceStore.allScores()
    if tmpSavedScores then
        savedScores = tmpSavedScores
    end
    local currentBestScore = savedScores[gameId]
    if currentBestScore then
        if score > currentBestScore then
            savedScores[gameId] = score
        end
    else
        savedScores[gameId] = score
    end
    

    local path = system.pathForFile(scores_file, base)
    local file = io.open(path, "w")
    if file then
        local contents = json.encode(savedScores)
        log('persistenceStore - saveScore - FILE FOUND - will save - ' .. contents)
        file:write(contents)
        io.close(file)
    end

end


persistenceStore.bestScore = function(gameId)

    log('persistenceStore - bestScore for ' .. gameId)
    local savedScores = persistenceStore.allScores()
    local savedScore = savedScores["gameId"]

    if not savedScore then
        savedScore = Runtime:dispatchEvent({ name = 'coronaView', event = 'getBestScore' })
        if savedScore then
            persistenceStore.saveScore(savedScore, gameId)
        end
    end

    if savedScore then
        log('persistenceStore - bestScore for ' .. gameId .. ' = ' .. savedScore)
    end

    return savedScore
end

local function bestScore(event) 
    log('score - bestScore ' .. event.score .. ' for game ' .. event.gameId)
    persistenceStore.saveScore(event.score, event.gameId)
end
Runtime:addEventListener('bestScore', bestScore)

---------------------------------------------------------------------------------

persistenceStore.allRevives = function()

    log('persistenceStore - allRevives')

    local path = system.pathForFile(revive_file, base)
    local contents = ""
    local savedRevives = {}
    local file = io.open(path, "r")

    if file then
        -- read all contents of file into a string
        local contents = file:read( "*a" )
        savedRevives = json.decode(contents)
        io.close( file )
    end

    return savedRevives
end

persistenceStore.didRevive = function(gameId, disabledTime)

    log('persistenceStore - didRevive for ' .. gameId)

    local savedRevives = {}
    local tmpSavedRevives = persistenceStore.allRevives()
    if tmpSavedRevives then
        savedRevives = tmpSavedRevives
    end
    local now = os.time()
    savedRevives[gameId] = now + disabledTime

    local path = system.pathForFile(revive_file, base)
    local file = io.open(path, "w")
    if file then
        local contents = json.encode(savedRevives)
        log('persistenceStore - didRevive - FILE FOUND - will save - ' .. contents .. ' with now = ' .. now)
        file:write(contents)
        io.close(file)
    end

end

persistenceStore.canRevive = function(gameId)

    log('persistenceStore - canRevive for ' .. gameId)
    local savedRevives = persistenceStore.allRevives()
    local savedRevive = savedRevives[gameId]

    if savedRevive and os.time() < savedRevive then
        log('persistenceStore - canRevive for ' .. gameId .. ' with savedRevive = ' .. savedRevive .. " - VS now = " .. os.time())
        return false
    end

    return true
end

return persistenceStore