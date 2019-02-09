-- The Computer Language Benchmarks Game
-- https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
-- originally contributed by Mike Pall

local BottomUpTree = nil
BottomUpTree = function(depth)
  if depth > 0 then
    depth = depth - 1
    local left, right = BottomUpTree(depth), BottomUpTree(depth)
    return { left, right }
  else
    return { }
  end
end

local ItemCheck = nil
ItemCheck = function(tree)
  if tree[1] then
    return 1 + ItemCheck(tree[1]) + ItemCheck(tree[2])
  else
    return 1
  end
end

local N = 15
local mindepth = 4
local maxdepth = mindepth + 2
if maxdepth < N then maxdepth = N end

do
  local stretchdepth = maxdepth + 1
  local stretchtree = BottomUpTree(stretchdepth)
  print("stretch tree of depth "..stretchdepth.."\t check: "..ItemCheck(stretchtree))
end

local longlivedtree = BottomUpTree(maxdepth)

for depth_ctr = mindepth/2, maxdepth/2 do
  local depth = 2 * depth_ctr
  local iterations = 2 ^ (maxdepth - depth + mindepth)
  local check = 0
  for i=1,iterations do
    check = check + ItemCheck(BottomUpTree(depth))
  end
  print(iterations.."\t trees of depth "..depth.."\t check: "..check)
end

print("long lived tree of depth "..maxdepth.."\t check: "..ItemCheck(longlivedtree))
