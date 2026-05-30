import { useEffect, useRef } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";

type PanoramaViewerProps = {
  /** Absolute or root-relative URL to an equirectangular panorama image. */
  src: string;
  className?: string;
  /** Initial vertical field of view in degrees (matches panorama-native default). */
  initialFov?: number;
};

const MIN_FOV = 45;
const MAX_FOV = 110;

/**
 * WebGL equirectangular panorama viewer — same idea as panorama-native's
 * inside-facing sphere mesh + drag look + scroll/wheel zoom.
 */
export function PanoramaViewer({
  src,
  className = "",
  initialFov = 90,
}: PanoramaViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container || !src) return;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(initialFov, 1, 0.1, 1000);
    camera.position.set(0, 0, 0);

    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.setClearColor(0x000000, 1);
    container.appendChild(renderer.domElement);

    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enablePan = false;
    controls.enableZoom = false;
    controls.enableDamping = true;
    controls.dampingFactor = 0.08;
    controls.rotateSpeed = -0.25;
    controls.target.set(0, 0, -1);
    controls.update();

    const onWheel = (event: WheelEvent) => {
      event.preventDefault();
      const delta = event.deltaY * 0.05;
      camera.fov = THREE.MathUtils.clamp(camera.fov + delta, MIN_FOV, MAX_FOV);
      camera.updateProjectionMatrix();
    };
    renderer.domElement.addEventListener("wheel", onWheel, { passive: false });

    const geometry = new THREE.SphereGeometry(500, 64, 40);
    geometry.scale(-1, 1, 1);

    let mesh: THREE.Mesh | null = null;
    let animationId = 0;

    const loader = new THREE.TextureLoader();
    loader.load(
      src,
      (texture) => {
        texture.colorSpace = THREE.SRGBColorSpace;
        texture.minFilter = THREE.LinearFilter;
        texture.magFilter = THREE.LinearFilter;
        const material = new THREE.MeshBasicMaterial({ map: texture });
        mesh = new THREE.Mesh(geometry, material);
        scene.add(mesh);
      },
      undefined,
      () => {
        const fallback = document.createElement("div");
        fallback.className =
          "absolute inset-0 flex items-center justify-center text-sm text-white/80 px-6 text-center";
        fallback.textContent = "Could not load panorama image.";
        container.appendChild(fallback);
      },
    );

    const resize = () => {
      const width = container.clientWidth;
      const height = container.clientHeight;
      if (width <= 0 || height <= 0) return;
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
      renderer.setSize(width, height, false);
    };

    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(container);
    resize();

    const animate = () => {
      animationId = window.requestAnimationFrame(animate);
      controls.update();
      renderer.render(scene, camera);
    };
    animate();

    return () => {
      window.cancelAnimationFrame(animationId);
      resizeObserver.disconnect();
      renderer.domElement.removeEventListener("wheel", onWheel);
      controls.dispose();
      if (mesh) {
        mesh.geometry.dispose();
        const material = mesh.material;
        if (material instanceof THREE.MeshBasicMaterial) {
          material.map?.dispose();
          material.dispose();
        }
      } else {
        geometry.dispose();
      }
      renderer.dispose();
      if (renderer.domElement.parentNode === container) {
        container.removeChild(renderer.domElement);
      }
    };
  }, [src, initialFov]);

  return (
    <div className={`relative w-full h-full min-h-[320px] bg-black ${className}`}>
      <div ref={containerRef} className="absolute inset-0" />
      <div className="pointer-events-none absolute bottom-3 left-1/2 -translate-x-1/2 rounded-full bg-black/60 px-3 py-1 text-xs text-white/90">
        Drag to look around · Scroll to zoom
      </div>
    </div>
  );
}
