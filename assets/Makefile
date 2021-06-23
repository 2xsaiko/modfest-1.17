RP_ROOT = ../src/main/resources/

all: hook.png cable.png gloves.png

%.png: %.xcf
	./xcf2png.sh $< $@

.PHONY: copy
copy: all
	cp hook.png $(RP_ROOT)/assets/trucc/textures/block/hook.png
	cp cable.png $(RP_ROOT)/assets/trucc/textures/cable.png
	cp gloves.png $(RP_ROOT)/assets/trucc/textures/item/gloves.png

.PHONY: clean
clean:
	rm -fv *.png
