local fields = {
    { x = 23, y = 78, charge = 0.32 },
    { x = 123, y = 578, charge = 0.33 },
    { x = 223, y = 678, charge = 0.34 },
    { x = 323, y = 778, charge = 0.35 },
    { x = 423, y = 478, charge = 0.36 },
    { x = 523, y = 178, charge = 0.37 },
    { x = 623, y = 278, charge = 0.38 },
    { x = 723, y = 378, charge = 0.39 },
    { x = 823, y = 878, charge = 0.40 },
    { x = 923, y = 978, charge = 0.41 },
}

local hash = 0
local function use(x)
    hash = (hash + x) * 0.99
end

local WIDTH, HEIGHT = 1000, 500

local math = math
local atan2 = math.atan2
local sin = math.sin
local cos = math.cos
local sqrt = math.sqrt

for x = 0, WIDTH do
    for y = 1, HEIGHT do
        local vx, vy = 0, 0
        for i = 1, #fields do
            local field = fields[i]
            local dx, dy = x - field.x, y - field.y
            local angle = atan2(dy, dx)
            local distance = sqrt(dx * dx + dy * dy)
            if distance == 0 then
                distance = 0.00001
            end
            local intensity = field.charge / distance / distance
            vx = vx + cos(angle) * intensity
            vy = vy + sin(angle) * intensity
        end
        local length = sqrt(vx * vx + vy * vy)
        use(length)
    end
end

print(hash)
