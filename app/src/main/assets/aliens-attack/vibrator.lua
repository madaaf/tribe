local exports = {}

local function vibrate(vibrationType)

	local event = { name='coronaView', event='vibrate', type=vibrationType }
	Runtime:dispatchEvent(event)
end

exports.send = function()
	vibrate()
end

exports.sendImpact = function()
	vibrate('impact')
end

return exports
