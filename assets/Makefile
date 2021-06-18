RP_ROOT = ../src/main/resources/

all: hook.png

%.png: %.xcf
	./xcf2png.sh $< $@

.PHONY: copy
copy: all
	cp hook.png $(RP_ROOT)/assets/trucc/textures/block/hook.png

.PHONY: clean
clean:
	rm -fv *.png