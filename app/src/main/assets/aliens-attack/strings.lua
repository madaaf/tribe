local strings = {
	default = {
		title = 'ALIENS\nATTACK',
		lets_go = 'TAP AN ALIEN TO KILL IT!',
		you_won = 'YOU WON!',
		someone_won = '%s WON!',
		you_lost = 'YOU LOST',
		someone_lost = '%s LOST!',
		pending_instructions = 'IT WILL RESTART WHEN\nSOMEONE WINS',
		waiting_instructions = 'IT WILL RESTART WHEN\nSOMEONE JOINS'
	},
	fr = {
		title = 'ALIENS\nATTACK',
		lets_go = 'TAP UN ALIEN POUR LE TUER !',
		you_won = 'TU AS GAGNÉ !',
		someone_won = '%s A GAGNÉ !',
		you_lost = 'TU AS PERDU',
		someone_lost = '%s A PERDU !',
		pending_instructions = 'LE JEU VA REDEMARRER QUAND\nUN JOUEUR GAGNE',
		waiting_instructions = 'LE JEU VA REDEMARRER QUAND\nUN JOUEUR ARRIVE'
	}
}

local locale = 'default'
if system.getPreference("locale", "language") == 'fr' then
	locale = 'fr'
end

return function (key)
	local value = strings[locale][key]
	if value then
		return value
	else
		return key
	end
end
