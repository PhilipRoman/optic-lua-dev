local fields = {
    { x = 23, y = 78, charge = 0.32 },
    { x = 123, y = 578, charge = 1.33 },
    { x = 223, y = 378, charge = 0.34 },
    { x = 323, y = 228, charge = 1.35 },
    { x = 423, y = 478, charge = 0.36 },
    { x = 523, y = 178, charge = 1.37 },
    { x = 473, y = 278, charge = 0.38 },
    { x = 373, y = 328, charge = 1.39 },
    { x = 173, y = 428, charge = 0.40 },
    { x = 273, y = 128, charge = 1.41 },
}

local WIDTH, HEIGHT = 500, 500

local matrix = {}

for x = 1, WIDTH do
    matrix[x] = {}
    for y = 1, HEIGHT do
        local vx, vy = 0, 0
        for _, field in ipairs(fields) do
            local dx, dy = x - field.x, y - field.y
            local angle = math.atan2(dy, dx)
            local distance = math.sqrt(dx ^ 2 + dy ^ 2)
            if distance == 0 then
                distance = 1
            end
            local intensity = field.charge / distance / distance
            vx = vx + math.cos(angle) * intensity
            vy = vy + math.sin(angle) * intensity
        end
        matrix[x][y] = { vx, vy }
    end
end

local sum = 0
for x = 1, WIDTH do
    for y = 1, HEIGHT do
        local vector = matrix[x][y]
        sum = sum + math.sqrt(vector[1] ^ 2 + vector[2] ^ 2)
    end
end
print(sum / (WIDTH * HEIGHT))
