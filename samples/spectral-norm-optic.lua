-- The Computer Language Benchmarks Game
-- https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
-- originally contributed by Mike Pall
-- this is an optimized version with some functions removed
local function Av(x, y, N)
    for i=1,N do
        local a = 0
        for j=1,N do 
        	local ij = i + j - 1
        	a = a + x[j] * (1.0 / (ij * (ij-1) * 0.5 + i))
        end
        y[i] = a
    end
end

local function Atv(x, y, N)
    for i=1,N do
        local a = 0
        for j=1,N do 
        	local ij = i + j - 1
        	a = a + x[j] * (1.0 / (ij * (ij-1) * 0.5 + j))
        end
        y[i] = a
    end
end

local N = 500
local u, v, t = {}, {}, {}
for i=1,N do u[i] = 1 end

for i=1,10 do
	Av(u, t, N)
	Atv(t, v, N)
	Av(v, t, N)
	Atv(t, u, N)
end

local vBv, vv = 0, 0
for i=1,N do
    local ui, vi = u[i], v[i]
    vBv = vBv + ui*vi
    vv = vv + vi*vi
end
local sqrt = math.sqrt
print(sqrt(vBv / vv))
