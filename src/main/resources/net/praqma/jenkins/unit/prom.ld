OUTPUT_ARCH ( m68k )

MEMORY
 {
  reserved1 (!A)  : ORIGIN = 0, LENGTH = 0x00FFFFF
  application (rx) : ORIGIN = 0x100000, LENGTH = 2M
  reserved2 (!A)  : ORIGIN = 0x0300000, LENGTH = 0x04FFFFF
  ram (w)     : ORIGIN = 0x0800000, LENGTH = 2M
 }

SECTIONS {
  . = 0x100000;
  evtsect : {
    _evtsect = .;
    *( evtsect )
  }
  . = 0x100400; 
  crc : {
    *(crc)
  } 
  . = 0x100408; 
  .entrycode : { *(entrycode) }
  .init : { KEEP (*(.init)) }
  .text : {
        CREATE_OBJECT_SYMBOLS
        *(.text)
               . = ALIGN(2);
        __preinit_array_start = .;
        KEEP (*(.preinit_array))
        __preinit_array_end = .;

        . = ALIGN(2);
        __init_array_start = .;
        KEEP (*(SORT(.init_array.*)))
        KEEP (*(.init_array))
        __init_array_end = .;

        . = ALIGN(2);
        KEEP (*crtbegin.o(.ctors))
        KEEP (*(EXCLUDE_FILE (*crtend.o) .ctors))
        KEEP (*(SORT(.ctors.*)))
        __CTOR_END__ = .;
        KEEP (*crtend.o(.ctors))

        . = ALIGN(2);
        KEEP(*(.fini))

        . = ALIGN(2);
        __fini_array_start = .;
        KEEP (*(.fini_array))
        KEEP (*(SORT(.fini_array.*)))
        __fini_array_end = .;

        KEEP (*crtbegin.o(.dtors))
        KEEP (*(EXCLUDE_FILE (*crtend.o) .dtors))
        KEEP (*(SORT(.dtors.*)))
        KEEP (*crtend.o(.dtors))

  }
  . = ALIGN(2);
  .strings : { *(.string) }
  . = ALIGN(2);
  .rodata : { *(.rodata*) }
  . = ALIGN(2);

    _flash_data = .;
  . = 0x0800000 ;
    .data : AT (_flash_data)
    {
        _data = .;
        *(vtable)
        *(.data*)
        _edata = .;
    }
  
    .stack : {
      _stack = .;
      . += 4096;
      *(.stack)
      _stackend = .;
      stack = .;
    }
    .bootvar : { *(.bootvar) *(bootdata) }
    _vars = .;
    .bss :
    {
        _bss = .;
        *(.bss*)
        *(COMMON)
        _ebss = .;
        _end = .;
    }
   heap : {
     *(heap)
     . += _heapsize;
      _heapend = .;
   }
}

/* end of allocated ram _end */
PROVIDE( _heap = _end );

INPUT(crtbegin.o)

INPUT(prom/testsoftfloat.o);
INPUT(prom/remainder.o);
INPUT(prom/runsf.o);
INPUT(prom/rtl.o);
INPUT(prom/new.o);
INPUT(prom/co_bitset.o);
INPUT(prom/crc32.o);
INPUT(prom/crc.o);
INPUT(prom/flsh_bios_amd.o);
INPUT(prom/flsh_bios_intel.o);
INPUT(prom/flsh_device.o);
INPUT(prom/flsh_device_amd.o);
INPUT(prom/flsh_device_intel.o);
INPUT(prom/flsh_device_ram.o);
INPUT(prom/flsh_manager.o);
INPUT(prom/flsh_memory.o);
INPUT(prom/krnl_astd.o);
INPUT(prom/krnl_atst.o);
INPUT(prom/krnl_alog.o);
INPUT(prom/krnl_aseq.o);
INPUT(prom/krnl_asto.o);
INPUT(prom/krnl_prmtst.o);
INPUT(prom/krnl_xwdh.o);
INPUT(prom/krnl_sngltn.o);
INPUT(prom/krnl_time.o);
INPUT(prom/krnl_epilog.o);
INPUT(prom/krnl_clkfrq.o);
INPUT(prom/krnl_evt.o);
INPUT(prom/krnl_evtasm.o);
INPUT(prom/krnl_intwd.o);
INPUT(prom/krnl_pit.o);
INPUT(prom/krnl_sci.o);
INPUT(prom/krnl_xcptin.o);
INPUT(prom/cbw_extwd.o);
INPUT(prom/cbw_inl.o);
INPUT(prom/cbw_outl.o);
INPUT(prom/cbw_outl3.o);
INPUT(prom/krnl_flmem.o);
INPUT(prom/krnl_flpar.o);
INPUT(prom/krnl_flstr.o);
INPUT(prom/krnl_stores.o);
INPUT(prom/krnl_rtc.o);
INPUT(prom/krnl_nclk_r.o);
INPUT(prom/krnl_ntime_r.o);
INPUT(prom/krnl_crttsk.o);
INPUT(prom/krnl_otmr.o);
INPUT(prom/krnl_stask.o);
INPUT(prom/krnl_crtreg_r.o);
INPUT(prom/krnl_curtask_r.o);
INPUT(prom/krnl_otmr_r.o);
INPUT(prom/krnl_queue_r.o);
INPUT(prom/krnl_res_r.o);
INPUT(prom/krnl_sema_r.o);
INPUT(prom/krnl_task_r.o);
INPUT(prom/krnl_taskid_r.o);
INPUT(prom/krnl_wait_r.o);
INPUT(prom/krnl_reset.o);
INPUT(prom/cclock.o);
INPUT(prom/cmbox.o);
INPUT(prom/cpart.o);
INPUT(prom/cqueue.o);
INPUT(prom/cres.o);
INPUT(prom/csema.o);
INPUT(prom/ctask.o);
INPUT(prom/format.o);
INPUT(prom/rtxcbug.o);
INPUT(prom/rtxc.o);
INPUT(prom/rtxcapi.o);
INPUT(prom/rtxcinit.o);
INPUT(prom/setvect.o);
INPUT(prom/rtxcasm.o);
INPUT(prom/krnl_adef.o);
INPUT(prom/krnl_sastd.o);
INPUT(prom/krnl_sasto.o);
INPUT(prom/krnl_satst.o);
INPUT(prom/krnl_scires.o);
INPUT(prom/krnl_scidrv_m.o);
INPUT(prom/krnl_sciisr_m.o);
INPUT(prom/krnl_heap_m.o);
INPUT(prom/krnl_init_m.o);
INPUT(prom/krnl_rclk_m.o);
INPUT(prom/krnl_rclkasm_m.o);
INPUT(prom/krnl_unex_m.o);
INPUT(prom/krnl_trcchnl.o);
INPUT(prom/entry.o);
INPUT(prom/entry_hooks.o);
INPUT(prom/ccbw_int.o);
INPUT(prom/ccbw_intasm.o);
INPUT(prom/ccbw_latch.o);
INPUT(prom/ccbw_outl0.o);
INPUT(prom/ccbw_outl1.o);
INPUT(prom/ccbw_outl2.o);
INPUT(prom/cppmain.o);
INPUT(prom/EEPromOnB.o);
INPUT(prom/EEProm.o);
INPUT(prom/listreg.o);
INPUT(prom/nctrl_resetcmd.o);
INPUT(prom/softfloat.o);
INPUT(prom/softfloatmap.o);

INPUT(crtend.o)

/*libraries*/

INPUT(-lstdc++)
INPUT(-lm)
INPUT(-lgcc)
INPUT(-lc)
INPUT(-lgcc)


