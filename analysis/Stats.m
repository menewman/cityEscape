function [ sampMean, sampVar, stdDev ] = Stats( values )
%Stats calculates sample statistics for a simulation of route evacuations
% use normal matlab functions
% calculate sample mean
sampMean = mean(values);

% calculate sample variance
sampVar = var(values);

% calculate standard deviation
stdDev = std(values);

% plot Gaussian distribution by the formula:
% f(x, sampVar, sampMean) = e^((-(x-sampMean)^2)/(2*sampVar^2))
x=-3:0.01:3;
fx=1/sqrt(2*pi)/stdDev*exp(-(x-sampMean).^2/2/stdDev/stdDev);
plot(x,fx)
end

