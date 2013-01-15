function experiment = statScript()
% carries out time-driven evacuation simulation for various population sizes and various average alertness

% initialize simulation output collecting arrays
dead = zeros(10, 1);
alive = zeros(10, 1);
escaped = zeros(10, 1);
stats = zeros(5, 5); % 5 population sizes, each exhibiting 5 different awareness levels

% for testing different awareness values, loop back to beginning of the population size increments
for int a = 1:1:5

	% execute simulation for 2^6-2^10 as population size
	for int i = 6:1:10

		% for each instance of simulation, take 10 total trials, and using that information, calculate one set of Stats.m values
		for int j = 1:1:10

			% somehow execute Simulation with an argument for initial population of 2^i
			dead(j) = value A;
			alive(j) = value B;
			escaped(j) = value C;

		end

		% accumulate statistics and plots gaussian distributions
		deadStats(:, a) = Stats(dead);
		aliveStats(:, a) = Stats(alive);
		escapedStats(:, a) = Stats(escaped);
	
    end
end

end
