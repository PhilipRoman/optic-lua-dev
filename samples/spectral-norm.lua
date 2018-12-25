-- The Computer Language Benchmarks Game
-- https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
-- originally contributed by Mike Pall

local function A(p_i, j)
    local i = 0+p_i
    --local j = 0+p_j
    local ij = i+j-1
    return 1.0 / (ij * (ij-1) * 0.5 + i)
end

local function Av(x, y, N)
    for i=1,N do
        local a = 0
        for j=1,N do a = a + x[j] * A(i, j) end
        y[i] = a
    end
end

local function Atv(x, y, N)
    for i=1,N do
        local a = 0
        for j=1,N do a = a + x[j] * A(j, i) end
        y[i] = a
    end
end

local function AtAv(x, y, t, p_N)
    local N = 0+p_N
    Av(x, t, N)
    Atv(t, y, N)
end

local N = 100
local u, v, t = {}, {}, {}
for i=1,N do u[i] = 1 end

for i=1,10 do AtAv(u, v, t, N) AtAv(v, u, t, N) end

local vBv, vv = 0, 0
for i=1,N do
    local ui, vi = u[i], v[i]
    vBv = vBv + ui*vi
    vv = vv + vi*vi
end
local sqrt = math.sqrt
print(sqrt(vBv / vv))
