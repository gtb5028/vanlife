## Docker Build and Push

```declarative
docker buildx create --use
docker buildx build --platform linux/amd64,linux/arm64 \
-t ghcr.io/tbeerbower/vanlife-backend:latest --push .
```
Note ARM64 is probably not needed.


## Deploy to Kubernetes

```bash
ssh <user>@192.168.1.90 # or whatever your server is
```

Apply each of the descriptors...
```bash

microk8s kubectl apply -f <file>.yaml
```

### Check How Your Service is Exposed

```bash
microk8s kubectl get services

NAME               TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)
vanlife-backend    LoadBalancer   10.152.183.200   192.168.1.90  9000:31234/TCP
```

If Using LoadBalancer and your service shows an EXTERNAL-IP like 192.168.1.90 and a mapped port (e.g., 31234), then your base_url is:

```bash
http://192.168.1.90:31234
```

