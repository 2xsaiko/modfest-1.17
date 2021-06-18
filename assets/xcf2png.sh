#!/bin/bash

{
  cat <<EOF
(define (convert-xcf-to-png filename outfile)
  (let* (
	 (image (car (gimp-file-load RUN-NONINTERACTIVE filename filename)))
	 (drawable (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE)))
	 )
    (file-png-save-defaults RUN-NONINTERACTIVE image drawable outfile outfile)
    (gimp-image-delete image) ; ... or the memory will explode
    )
  )

(gimp-message-set-handler 1) ; Messages to standard output
EOF

  echo "(convert-xcf-to-png \"$1\" \"$2\")"
  echo "(gimp-quit 0)"
} | gimp -i -b -
