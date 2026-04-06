import re

filepath = r"d:\project\eden-nutrition\eden-service\src\main\java\eden\service\impl\SeckillServiceImpl.java"

with open(filepath, "r", encoding="utf-8") as f:
    text = f.read()

# Replace seckillProductMapper usage in B-end methods to seckillMapper
# Let's locate the B-end bounded area
idx = text.find("// ==================== B端后台管理方法 ====================")
if idx != -1:
    top = text[:idx]
    bottom = text[idx:]

    # Fixing getAdminDetail
    bottom = bottom.replace("seckillProductMapper.selectById(id)", "seckillMapper.selectById(id)")
    
    # Fixing addAdminSeckill
    bottom = bottom.replace("seckillProductMapper.insert(sp);", "seckillMapper.insert(sp);")
    
    # Fixing updateAdminSeckill
    bottom = bottom.replace("seckillProductMapper.update(sp);", "seckillMapper.update(sp);")
    
    # Fixing deleteAdminSeckill
    bottom = bottom.replace("seckillProductMapper.updateStatus(id, 2);", """SeckillProduct tempSp = new SeckillProduct();
        tempSp.setId(id);
        tempSp.setStatus(2);
        seckillMapper.update(tempSp);""")

    # Fixing finishAdminSeckill
    bottom = bottom.replace("seckillProductMapper.update(product);", "seckillMapper.update(product);")

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(top + bottom)
    print("Fixed!")
else:
    print("Not found!")