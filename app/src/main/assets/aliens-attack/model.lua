local model = {}

model.levels = {
	easy = {
		min 	    = 0,
		popInterval = function () return 0.3 + math.random() * 0.2 end,
		speed 		= function () return 3.0 + math.random() * 2.0 end,
		background  = 0,
		soundtrack  = 1,
	},
	medium = {
		min 		= 50,
		popInterval = function () return 0.25 + math.random() * 0.2 end,
		speed 		= function () return 2.5  + math.random() * 2.0 end,
		background  = 1,
		soundtrack  = 2,
	},
	hard = {
		min 		= 100,
		popInterval = function () return 0.2 + math.random() * 0.2 end,
		speed 		= function () return 2.0 + math.random() * 2.0 end,
		background  = 2,
		soundtrack  = 3,
	},
	extreme = {
		min 	    = 150,
		popInterval = function () return 0.15 + math.random() * 0.2 end,
		speed 		= function () return 1.5  + math.random() * 2.0 end,
		background  = 3,
		soundtrack  = 1,
	},
	alien = {
		min 		= 200,
		popInterval = function () return  0.1 + math.random() * 0.2 end,
		speed 		= function () return  1.5 + math.random() * 1.5 end,
		background  = 4,
		soundtrack  = 2,
	},
}

model.levelByScore = function(score)

	if score < model.levels.medium.min then
		return model.levels.easy
	elseif score < model.levels.hard.min then
		return model.levels.medium
	elseif score < model.levels.extreme.min then
		return model.levels.hard
	elseif score < model.levels.alien.min then
		return model.levels.extreme
	else
		return model.levels.alien
	end
end

return model
