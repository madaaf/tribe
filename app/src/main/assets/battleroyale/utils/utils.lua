math.randomseed( os.time() )

local exports = {}

exports.generateId = function()
	return '' .. math.random(1000000)
end

exports.tablelength = function(T)
  local count = 0
  for _ in pairs(T) do count = count + 1 end
  return count
end

return exports