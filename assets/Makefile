RP_ROOT = ../src/main/resources/

ITEM_TEXTURES = gloves
BLOCK_TEXTURES = hook motor_flat motor_front motor_inner motor_inner_top motor_left motor_right
MISC_TEXTURES = cable

ITEM_TEXTURES_PNG = $(ITEM_TEXTURES:=.png)
BLOCK_TEXTURES_PNG = $(BLOCK_TEXTURES:=.png)
MISC_TEXTURES_PNG = $(MISC_TEXTURES:=.png)

all: $(MISC_TEXTURES_PNG) $(ITEM_TEXTURES_PNG) $(BLOCK_TEXTURES_PNG)

%.png: %.xcf
	./xcf2png.sh $< $@

.PHONY: copy
copy: all
	cp $(BLOCK_TEXTURES_PNG) $(RP_ROOT)/assets/trucc/textures/block/
	cp $(ITEM_TEXTURES_PNG) $(RP_ROOT)/assets/trucc/textures/item/
	cp $(MISC_TEXTURES_PNG) $(RP_ROOT)/assets/trucc/textures/

.PHONY: clean
clean:
	rm -v *.png || true
